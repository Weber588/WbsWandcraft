package wbs.wandcraft.resourcepack;

import com.google.gson.Gson;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsFileUtil;
import wbs.utils.util.configuration.WbsConfigReader;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WandcraftSettings;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.util.ItemUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static wbs.wandcraft.resourcepack.ResourcePackObjects.ItemSelectorDefinition;

public class ResourcePackBuilder {
    public static final String ASSETS_FOLDER = "resourcepack/assets/";
    public static final @NotNull String NAMESPACE_ASSETS_FOLDER = ASSETS_FOLDER + WbsWandcraft.getInstance().namespace() + "/";
    public static final String ITEMS_FOLDER = ASSETS_FOLDER + "minecraft/items/";
    public static final @NotNull String MODELS_FOLDER = NAMESPACE_ASSETS_FOLDER + "models/";
    public static final String ITEM_MODELS_PATH = MODELS_FOLDER + "item/";
    public static final String BLOCK_MODELS_PATH = MODELS_FOLDER + "block/";
    public static final @NotNull String TEXTURES_FOLDER = NAMESPACE_ASSETS_FOLDER + "textures/";
    public static final String ITEM_TEXTURES_PATH = TEXTURES_FOLDER + "item/";
    public static final String BLOCK_TEXTURES_PATH = TEXTURES_FOLDER + "block/";

    public static void loadResourcePack(WandcraftSettings settings, YamlConfiguration config) {
        createResourcePack(settings, config);
        writeToExternalPlugins();
    }

    private static void writeToExternalPlugins() {
        WbsWandcraft plugin = WbsWandcraft.getInstance();

        String name = plugin.getName();
        String namespace = name.toLowerCase();

        if (Bukkit.getPluginManager().getPlugin("ResourcePackManager") != null) {
            plugin.getComponentLogger().info(Component.text("ResourcePackManager detected! Injecting resource pack.").color(NamedTextColor.GREEN));
            plugin.getComponentLogger().info(Component.text("Note: This will load last unless you add \"" + name + "\" to the priority list in ResourcePackManager/config.yml").color(NamedTextColor.GREEN));

            try {
                Files.copy(plugin.getDataPath().resolve(
                                namespace + "_resource_pack.zip"),
                        Path.of("plugins/ResourcePackManager/mixer/" + namespace + "_resource_pack.zip"),
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to copy resource pack to ResourcePackManager/mixer!");
            }
            /*
            ResourcePackManagerAPI.registerResourcePack(
                    getName(),
                    "WbsWandcraft/wbswandcraft_resource_pack.zip",
                    false,
                    false,
                    true,
                    true,
                    "wbswandcraft:wandcraft reload"
            );
             */
        }
    }

    private static void createResourcePack(WandcraftSettings settings, YamlConfiguration config) {
        WbsWandcraft plugin = WbsWandcraft.getInstance();

        String name = plugin.getName();
        String namespace = name.toLowerCase();

        ConfigurationSection modelsSection = config.getConfigurationSection("item-models");
        if (modelsSection != null) {
            ConfigurationSection vanillaModelsSection = config.getConfigurationSection("vanilla-models");
            if (vanillaModelsSection != null) {
                for (String key : vanillaModelsSection.getKeys(false)) {
                    settings.registerItemModel(key, WbsConfigReader.getNamespacedKey(vanillaModelsSection, key));
                }
            }

            Set<String> resourcesToLoad = new HashSet<>();

            resourcesToLoad.add("resourcepack/pack.mcmeta");

            Gson gson = new Gson();

            resourcesToLoad.addAll(writeProviders(gson, WandcraftRegistries.SPELLS.stream().toList(), ItemUtils.BASE_MATERIAL_SPELL));
            resourcesToLoad.addAll(writeProviders(gson, WandcraftRegistries.ATTRIBUTES.stream().toList(), ItemUtils.BASE_MATERIAL_MODIFIER));
            resourcesToLoad.addAll(writeProviders(gson, WandcraftRegistries.WAND_MODELS.stream().toList(), ItemUtils.BASE_MATERIAL_WAND));
            //resourcesToLoad.addAll(writeProviders(gson, List.of(new SpellbookItemTextureProvider()), ItemUtils.DISPLAY_MATERIAL_SPELLBOOK));
            resourcesToLoad.addAll(writeProviders(gson, List.of(new SpellbookItemModelProvider()), ItemUtils.DISPLAY_MATERIAL_SPELLBOOK));
            resourcesToLoad.addAll(writeProviders(gson, List.of(getSimpleProvider("blank_scroll")), ItemUtils.BASE_MATERIAL_BLANK_SCROLL));

            resourcesToLoad.addAll(writeProviders(gson, WandcraftRegistries.HAT_TEXTURES.stream().toList(), ItemUtils.DISPLAY_MATERIAL_HAT, true));

            resourcesToLoad.forEach(path -> {
                if (plugin.getResource(path) != null) {
                    plugin.saveResource(path, true);
                } else {
                    plugin.getLogger().severe("The resource at path \"" + path + "\" was not found! The vanilla texture will be used.");
                }
            });

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

    private static <T extends ItemModelProvider> Set<String> writeProviders(Gson gson, List<T> providers, Material baseMaterial) {
        return writeProviders(gson, providers, baseMaterial, false);
    }
    private static <T extends ItemModelProvider> Set<String> writeProviders(Gson gson, List<T> providers, Material baseMaterial, boolean isBlock) {
        Set<String> resourcesToLoad = new HashSet<>();

        WbsWandcraft plugin = WbsWandcraft.getInstance();

        Set<T> valid = new HashSet<>();
        providers.forEach(provider -> {
            if (provider instanceof ExternalItemProvider externalProvider) {
                String folder = externalProvider.getModelType();
                String modelsFolder = MODELS_FOLDER + folder + "/";

                String modelPath = modelsFolder + provider.value() + ".json";
                resourcesToLoad.add(modelPath);

                for (String additionalModel : externalProvider.getAdditionalModels()) {
                    resourcesToLoad.add(modelsFolder + additionalModel + ".json");
                }

                String texturesFolder = TEXTURES_FOLDER + folder + "/";
                String texturePath = texturesFolder + provider.value() + ".png";
                resourcesToLoad.add(texturePath);

                for (String additionalTexture : externalProvider.getAdditionalTextures()) {
                    resourcesToLoad.add(texturesFolder + additionalTexture + ".png");
                }

                if (plugin.getResource(modelPath) != null && plugin.getResource(texturePath) != null) {
                    valid.add(provider);
                }
            }

            if (provider instanceof DynamicItemTextureProvider itemProvider) {
                itemProvider.getModelDefinitions().forEach((name, definition) -> {
                    writeJSONToFile(
                            plugin.getDataPath().resolve(ResourcePackBuilder.ITEM_MODELS_PATH),
                            name,
                            gson,
                            definition
                    );
                });

                for (TextureLayer texture : itemProvider.getTextures()) {
                    String imagePath = ITEM_TEXTURES_PATH + texture.name() + ".png";

                    if (plugin.getResource(imagePath) != null) {
                        valid.add(provider);
                    }

                    resourcesToLoad.add(imagePath);
                    if (texture.isAnimated()) {
                        String metaPath = ITEM_TEXTURES_PATH + texture.name() + ".png.mcmeta";
                        resourcesToLoad.add(metaPath);
                    }
                }
            }
        });

        writeJSONToFile(
                plugin.getDataPath().resolve(ResourcePackBuilder.ITEMS_FOLDER),
                baseMaterial.key().value(),
                gson,
                new ItemSelectorDefinition(
                        baseMaterial,
                        valid.stream().toList(),
                        isBlock
                )
        );

        return resourcesToLoad;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void writeJSONToFile(Path folder, String key, Gson gson, Object object) {
        try {
            File file = folder.resolve(key + ".json").toFile();
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

    private static DynamicItemTextureProvider getSimpleProvider(String name) {
        return new DynamicItemTextureProvider() {
            @Override
            public @NotNull List<TextureLayer> getTextures() {
                return List.of(
                        new TextureLayer(name)
                );
            }

            @Override
            public @NotNull NamespacedKey getKey() {
                return WbsWandcraft.getKey(name);
            }
        };
    }
}
