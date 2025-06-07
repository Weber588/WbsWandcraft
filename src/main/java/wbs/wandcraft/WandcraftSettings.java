package wbs.wandcraft;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.WbsFileUtil;
import wbs.utils.util.configuration.WbsConfigReader;
import wbs.utils.util.plugin.WbsSettings;
import wbs.wandcraft.crafting.ArtificingConfig;
import wbs.wandcraft.spell.modifier.ModifierScope;
import wbs.wandcraft.util.ItemUtils;

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
        return getItemModel(null, names);
    }
    @Nullable
    @Contract("!null, _ -> !null")
    public NamespacedKey getItemModel(@Nullable NamespacedKey defaultKey, String ... names) {
        for (String name : names) {
            NamespacedKey key = itemModels.get(name);
            if (key != null) {
                return key;
            }
        }

        return defaultKey;
    }

    @Override
    public void reload() {
        YamlConfiguration config = loadDefaultConfig("config.yml");

        loadResourcePack(config);

        ConfigurationSection artificingTableSection = config.getConfigurationSection("artificing-table");
        if (artificingTableSection != null) {
            artificingConfig = new ArtificingConfig(artificingTableSection, this, "config.yml/artificing-table");
        }
    }

    private void loadResourcePack(YamlConfiguration config) {
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

                writeJSONToFile(plugin.getDataPath().resolve(itemsFolder), ItemUtils.BASE_MATERIAL_SPELL.getKey(), gson, new ItemSelectorDefinition(
                        ItemUtils.BASE_MATERIAL_SPELL,
                        WandcraftRegistries.SPELLS.stream().toList())
                );

                writeJSONToFile(plugin.getDataPath().resolve(itemsFolder), ItemUtils.BASE_MATERIAL_MODIFIER.getKey(), gson, new ItemSelectorDefinition(
                        ItemUtils.BASE_MATERIAL_MODIFIER,
                        Arrays.stream(ModifierScope.values()).toList())
                );

                writeJSONToFile(plugin.getDataPath().resolve(itemsFolder), ItemUtils.BASE_MATERIAL_WAND.getKey(), gson,
                        new ItemSelectorDefinition(
                                ItemUtils.BASE_MATERIAL_WAND,
                                WandcraftRegistries.WAND_TEXTURES.stream().toList(),
                                List.of(new ModelTint())
                        )
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

                WandcraftRegistries.WAND_TEXTURES.stream().forEach(texture -> {
                    NamespacedKey key = texture.getKey();

                    String texturePath = key.getNamespace() + ":item/" + texture.getTexture();
                    String baseTexture = key.getNamespace() + ":item/" + texture.getBaseTexture();
                    writeJSONToFile(plugin.getDataPath().resolve(itemModelsPath), key, gson, new ModelDefinition(
                            "minecraft:item/handheld",
                            Map.of(
                                    "layer0", texturePath,
                                    "layer1", baseTexture
                            )
                    ));
                    resourcesToLoad.add(texturesPath + texture.getTexture() + ".png");
                    resourcesToLoad.add(texturesPath + texture.getBaseTexture() + ".png");
                    if (texture.isAnimated()) {
                        resourcesToLoad.add(texturesPath + texture.getTexture() + ".png.mcmeta");
                    }
                    if (texture.isBaseAnimated()) {
                        resourcesToLoad.add(texturesPath + texture.getBaseTexture() + ".png.mcmeta");
                    }
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
            this(baseMaterial, definitions, null);
        }
        private ItemSelectorDefinition(Material baseMaterial, List<? extends TextureProvider> definitions, @Nullable List<ModelTint> tints) {
            StaticModel fallback = new StaticModel("minecraft:item/" + baseMaterial.key().value(), null);

            SelectModel model = new SelectModel("custom_model_data", 0, fallback);

            for (TextureProvider definition : definitions) {
                Key key = definition.key();
                model.addCase(new ModelCase(
                        key.asString(),
                        new StaticModel(key.namespace() + ":item/" + key.value(), tints)
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
        private final List<ModelTint> tints;

        private StaticModel(String model, List<ModelTint> tints) {
            super("minecraft:model");
            this.model = model;
            this.tints = tints;
        }
    }

    private static final class ModelTint {
        private final String type = "minecraft:custom_model_data";
        private final int index = 0;
        @SerializedName("default")
        private final int defaultValue = 32768;
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
