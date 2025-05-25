package wbs.wandcraft;

import com.google.gson.Gson;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.WbsFileUtil;
import wbs.utils.util.configuration.WbsConfigReader;
import wbs.utils.util.plugin.WbsSettings;
import wbs.wandcraft.commands.CommandBuildModifier;
import wbs.wandcraft.commands.CommandBuildSpell;
import wbs.wandcraft.crafting.ArtificingConfig;
import wbs.wandcraft.spell.modifier.ModifierScope;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class WandcraftSettings extends WbsSettings {
    protected WandcraftSettings(WbsWandcraft plugin) {
        super(plugin);
    }

    private final Map<String, @Nullable NamespacedKey> itemModels = new HashMap<>();
    private ArtificingConfig artificingConfig;
    public ArtificingConfig getArtificingConfig() {
        return artificingConfig;
    }

    private boolean useResourcePack;
    public boolean useResourcePack() {
        return useResourcePack;
    }

    @Nullable
    public NamespacedKey getItemModel(String ... names) {
        for (String name : names) {
            NamespacedKey key = itemModels.get(name);
            if (key != null) {
                return key;
            }
        }

        return null;
    }

    @Override
    public void reload() {
        YamlConfiguration config = loadDefaultConfig("config.yml");

        ConfigurationSection modelsSection = config.getConfigurationSection("item-models");
        if (modelsSection != null) {
            ConfigurationSection vanillaModelsSection = config.getConfigurationSection("vanilla-models");
            if (vanillaModelsSection != null) {
                for (String key : vanillaModelsSection.getKeys(false)) {
                    itemModels.put(key, WbsConfigReader.getNamespacedKey(vanillaModelsSection, key));
                }
            }

            useResourcePack = modelsSection.getBoolean("use-resource-pack", false);

            if (useResourcePack) {
                String namespace = plugin.getName().toLowerCase();
                Set<String> resourcesToLoad = new HashSet<>();

                resourcesToLoad.add("resourcepack/pack.mcmeta");

                String itemsFolder = "resourcepack/assets/minecraft/items/";
                String itemModelsPath = "resourcepack/assets/" + namespace + "/models/item/";
                String texturesPath = "resourcepack/assets/" + namespace + "/textures/item/";

                Gson gson = new Gson();

                writeJSONToFile(plugin.getDataPath().resolve(itemsFolder), CommandBuildSpell.BASE_MATERIAL.getKey(), gson, new ItemSelectorDefinition(
                        CommandBuildSpell.BASE_MATERIAL,
                        WandcraftRegistries.SPELLS.stream().toList())
                );

                writeJSONToFile(plugin.getDataPath().resolve(itemsFolder), CommandBuildModifier.BASE_MATERIAL.getKey(), gson, new ItemSelectorDefinition(
                        CommandBuildModifier.BASE_MATERIAL,
                        Arrays.stream(ModifierScope.values()).toList())
                );

                resourcesToLoad.add(itemModelsPath + "blank_scroll.json");
                resourcesToLoad.add(texturesPath + "blank_scroll.png");

                WandcraftRegistries.SPELLS.stream().forEach(definition -> {
                    NamespacedKey key = definition.getKey();

                    writeJSONToFile(plugin.getDataPath().resolve(itemModelsPath), key, gson, new ModelDefinition(
                            "minecraft:item/generated",
                            Map.of("layer0", key.getNamespace() + ":item/" + definition.getTexture())
                    ));
                    resourcesToLoad.add(texturesPath + definition.getTexture() + ".png");
                });

                resourcesToLoad.forEach(path -> plugin.saveResource(path, true));

                try {
                    WbsFileUtil.zipFolder(
                            plugin.getDataFolder().toPath().resolve("resourcepack").toFile(),
                            plugin.getDataFolder().toPath().resolve(namespace + "_resource_pack.zip").toString()
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        ConfigurationSection artificingTableSection = config.getConfigurationSection("artificing-table");
        if (artificingTableSection != null) {
            artificingConfig = new ArtificingConfig(artificingTableSection, this, "config.yml/artificing-table");
        }
    }

    private static void writeJSONToFile(Path folder, NamespacedKey key, Gson gson, Object object) {
        try {
            File file = folder.resolve(key.getKey() + ".json").toFile();
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (!file.exists()) {
                file.createNewFile();
            }
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(object, writer);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private record ModelDefinition(String parent, Map<String, String> textures) {}

    private static final class ItemSelectorDefinition {
        @SuppressWarnings({"unused", "FieldCanBeLocal"})
        private final Model model;

        private ItemSelectorDefinition(Material baseMaterial, List<? extends TextureProvider> definitions) {
            SelectModel model = new SelectModel("custom_model_data", 0, new StaticModel("minecraft:item/" + baseMaterial.key().value()));

            for (TextureProvider definition : definitions) {
                Key key = definition.key();
                model.addCase(new ModelCase(
                        key.asString(),
                        new StaticModel(key.namespace() + ":item/" + key.value())
                ));
            }

            this.model = model;
        }
    }

    private abstract static class Model {
        private final String type;

        private Model(String type) {
            this.type = type;
        }
    }

    private static class SelectModel extends Model {
        private final String property;
        private final int index;
        private final List<ModelCase> cases = new LinkedList<>();
        private final Model fallback;

        private SelectModel(String property, int index, Model fallback) {
            super("select");
            this.property = property;
            this.index = index;
            this.fallback = fallback;
        }

        private void addCase(ModelCase modelCase) {
            cases.add(modelCase);
        }
    }

    private static class StaticModel extends Model {
        private final String model;

        private StaticModel(String model) {
            super("minecraft:model");
            this.model = model;
        }
    }

    private static final class ModelCase {
        private final String when;
        private final Model model;

        private ModelCase(String when, Model model) {
            this.when = when;
            this.model = model;
        }
    }
}
