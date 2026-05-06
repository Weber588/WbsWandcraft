package wbs.wandcraft.resourcepack;

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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        boolean updatedPack = createResourcePack(settings, config);
        if (updatedPack) {
            writeToExternalPlugins();
        }
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

    private static boolean createResourcePack(WandcraftSettings settings, YamlConfiguration config) {
        long startTimestamp = System.currentTimeMillis();

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

            resourcesToLoad.addAll(writeProviders(WandcraftRegistries.SPELLS.stream().toList(), ItemUtils.BASE_MATERIAL_SPELL));
            resourcesToLoad.addAll(writeProviders(WandcraftRegistries.ATTRIBUTES.stream().toList(), ItemUtils.BASE_MATERIAL_MODIFIER));
            resourcesToLoad.addAll(writeProviders(WandcraftRegistries.WAND_MODELS.stream().toList(), ItemUtils.BASE_MATERIAL_WAND));
            //resourcesToLoad.addAll(writeProviders(gson, List.of(new SpellbookItemTextureProvider()), ItemUtils.DISPLAY_MATERIAL_SPELLBOOK));
            resourcesToLoad.addAll(writeProviders(List.of(new SpellbookItemModelProvider()), ItemUtils.DISPLAY_MATERIAL_SPELLBOOK));
            resourcesToLoad.addAll(writeProviders(List.of(getSimpleProvider("blank_scroll")), ItemUtils.BASE_MATERIAL_BLANK_SCROLL));

            resourcesToLoad.addAll(writeProviders(WandcraftRegistries.HAT_TEXTURES.stream().toList(), ItemUtils.DISPLAY_MATERIAL_HAT, true));

            boolean debugMode = WbsWandcraft.getInstance().getSettings().debugMode();

            List<String> missingPaths = new LinkedList<>();
            resourcesToLoad.forEach(path -> {
                if (plugin.getResource(path) != null) {
                    plugin.saveResource(path, debugMode);
                } else {
                    missingPaths.add(path);
                }
            });

            if (!missingPaths.isEmpty() && debugMode) {
                plugin.getLogger().severe("The following resources were not found! Default textures will be used.\n" +
                        String.join("\n\t- ", missingPaths));
            }

            File folderPath = plugin.getDataFolder().toPath().resolve("resourcepack").toFile();

            long lastModified = WbsFileUtil.getLastModifiedRecursive(folderPath);

            if (lastModified >= startTimestamp) {
                plugin.getLogger().info("Detected changes to resource pack -- updating! (%s >= %s)".formatted(lastModified, startTimestamp));

                try {
                    WbsFileUtil.zipFolder(
                            folderPath,
                            plugin.getDataFolder().toPath().resolve(namespace + "_resource_pack.zip").toString()
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                return true;
            }
        }
        return false;
    }

    private static <T extends ItemModelProvider> List<String> writeProviders(List<T> providers, Material baseMaterial) {
        return writeProviders(providers, baseMaterial, false);
    }
    private static <T extends ItemModelProvider> List<String> writeProviders(List<T> providers, Material baseMaterial, boolean isBlock) {
        List<String> resourcesToLoad = new LinkedList<>();

        WbsWandcraft plugin = WbsWandcraft.getInstance();

        List<T> valid = new LinkedList<>();
        for (T provider : providers) {
            boolean isValid = false;

            if (provider instanceof ExternalItemProvider externalProvider) {
                isValid |= handleExternalItemProvider(externalProvider, resourcesToLoad);
            }

            if (provider instanceof DynamicItemTextureProvider itemProvider) {
                isValid |= handleDynamicItemProvider(itemProvider, resourcesToLoad);
            }

            if (isValid) {
                valid.add(provider);
            }
        }

        valid = valid.stream().distinct().collect(Collectors.toCollection(LinkedList::new));

        Path externalItemFolder = plugin.getDataPath().resolve(ResourcePackBuilder.ITEMS_FOLDER);
        WbsFileUtil.writeJSONToFile(
                externalItemFolder.resolve(baseMaterial.key().value()).toFile(),
                new ItemSelectorDefinition(
                        baseMaterial,
                        valid.stream().toList(),
                        isBlock
                )
        );

        return resourcesToLoad.stream().distinct().collect(Collectors.toCollection(LinkedList::new));
    }

    private static boolean handleDynamicItemProvider(DynamicItemTextureProvider itemProvider, List<String> resourcesToLoad) {
        WbsWandcraft plugin = WbsWandcraft.getInstance();

        // TODO: Only save model file if valid
        itemProvider.getModelDefinitions().forEach((name, definition) -> {
            Path modelPath = plugin.getDataPath().resolve(ResourcePackBuilder.ITEM_MODELS_PATH);
            WbsFileUtil.writeJSONToFile(
                    modelPath.resolve(name).toFile(),
                    definition
            );
        });

        boolean isValid = false;

        for (TextureLayer texture : itemProvider.getTextures()) {
            String imagePath = ITEM_TEXTURES_PATH + texture.name() + ".png";

            if (plugin.getResource(imagePath) != null) {
                isValid = true;
            }

            // TODO: Only add resources to load if these are true
            resourcesToLoad.add(imagePath);
            if (texture.isAnimated()) {
                String metaPath = ITEM_TEXTURES_PATH + texture.name() + ".png.mcmeta";
                resourcesToLoad.add(metaPath);
            }
        }

        return isValid;
    }

    private static boolean handleExternalItemProvider(ExternalItemProvider externalProvider, List<String> resourcesToLoad) {
        WbsWandcraft plugin = WbsWandcraft.getInstance();

        String folder = externalProvider.getModelType();
        String modelsFolder = MODELS_FOLDER + folder + "/";

        String modelPath = modelsFolder + externalProvider.value() + ".json";
        resourcesToLoad.add(modelPath);

        for (String additionalModel : externalProvider.getAdditionalModels()) {
            resourcesToLoad.add(modelsFolder + additionalModel + ".json");
        }

        String texturesFolder = TEXTURES_FOLDER + folder + "/";
        String texturePath = texturesFolder + externalProvider.value() + ".png";
        resourcesToLoad.add(texturePath);

        for (String additionalTexture : externalProvider.getAdditionalTextures()) {
            resourcesToLoad.add(texturesFolder + additionalTexture + ".png");
        }

        // TODO: Only add resources to load if these are true
        return plugin.getResource(modelPath) != null && plugin.getResource(texturePath) != null;
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
