package wbs.wandcraft.resourcepack;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import net.kyori.adventure.key.Key;
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
import wbs.wandcraft.resourcepack.ResourcePackObjects.ItemDefinition;
import wbs.wandcraft.util.ItemUtils;
import wbs.wandcraft.wand.WandHolder;
import wbs.wandcraft.wand.background.WandBackground;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

public class ResourcePackBuilder {
    public static final int SPACE_CHAR_ZERO = 0xF600;

    public static final String ASSETS_FOLDER = "resourcepack/assets/";
    public static final @NotNull String WANDCRAFT = WbsWandcraft.getInstance().namespace();
    public static final @NotNull String MINECRAFT = NamespacedKey.MINECRAFT;

    public static String getAssetsFolder(String namespace, String folder) {
        return ASSETS_FOLDER + namespace + "/" + folder + "/";
    }

    public static String getItemsFolder(String namespace) {
        return getAssetsFolder(namespace, "items");
    }

    public static String getFontFolder(String namespace) {
        return getAssetsFolder(namespace, "font");
    }

    public static String getModelsFolder(String namespace, String subfolder) {
        return getAssetsFolder(namespace, "models/" + subfolder);
    }

    public static String getItemModelsFolder(String namespace) {
        return getModelsFolder(namespace, "item");
    }

    public static String getBlockModelsFolder(String namespace) {
        return getModelsFolder(namespace, "block");
    }

    public static String getTexturesFolder(String namespace) {
        return getAssetsFolder(namespace, "textures");
    }
    public static String getTexturesFolder(String namespace, String subfolder) {
        return getAssetsFolder(namespace, "textures/" + subfolder);
    }

    public static final SpellbookItemModelProvider ITEM_PROVIDER_SPELLBOOK = new SpellbookItemModelProvider();

    public static void loadResourcePack(WandcraftSettings settings, YamlConfiguration config) {
        boolean updatedPack = createResourcePack(settings, config);
        if (updatedPack) {
            writeToExternalPlugins();
        }
    }

    public static void writeToExternalPlugins() {
        WbsWandcraft plugin = WbsWandcraft.getInstance();
        WandcraftSettings settings = plugin.getSettings();

        String name = plugin.getName();
        String namespace = name.toLowerCase();

        if (Bukkit.getPluginManager().getPlugin("ResourcePackManager") != null) {
            plugin.getComponentLogger().info(Component.text("ResourcePackManager detected! Injecting resource pack.").color(NamedTextColor.GREEN));
            plugin.getComponentLogger().info(Component.text("Note: This will load last unless you add \"" + name + "\" to the priority list in ResourcePackManager/config.yml").color(NamedTextColor.GREEN));

            try {
                String fileName = namespace + "_resource_pack.zip";
                Path source = plugin.getDataPath().resolve(fileName);
                Path target = Path.of("plugins/ResourcePackManager/mixer/" + fileName);

                Files.createDirectories(target.getParent());

                Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to copy resource pack to ResourcePackManager/mixer!");
                if (settings.debugMode()) {
                    e.printStackTrace();
                }
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

        Set<String> resourcesToLoad = new HashSet<>();
        resourcesToLoad.add("resourcepack/pack.mcmeta");

        boolean debugMode = WbsWandcraft.getInstance().getSettings().debugMode();
        ConfigurationSection modelsSection = config.getConfigurationSection("item-models");
        if (modelsSection != null) {
            ConfigurationSection vanillaModelsSection = config.getConfigurationSection("vanilla-models");
            if (vanillaModelsSection != null) {
                for (String key : vanillaModelsSection.getKeys(false)) {
                    settings.registerItemModel(key, WbsConfigReader.getNamespacedKey(vanillaModelsSection, key));
                }
            }

            // Wand inventory items
            List<ItemModelProvider> outlineMaterialProviders = new LinkedList<>(WandcraftRegistries.WAND_BACKGROUNDS.stream().toList());
            outlineMaterialProviders.add(new ExistingItemProvider(NamespacedKey.minecraft("air"), "item"));
            resourcesToLoad.addAll(writeItemProviders(
                    outlineMaterialProviders,
                    false,
                    true,
                    WandHolder.MATERIAL_MAIN_OUTLINE,
                    WandHolder.MATERIAL_SECONDARY_OUTLINE,
                    WandHolder.MATERIAL_SLOT_LABEL
            ));

            resourcesToLoad.addAll(writeItemProviders(WandcraftRegistries.SPELLS.stream().toList(), ItemUtils.BASE_MATERIAL_SPELL));
            resourcesToLoad.addAll(writeItemProviders(WandcraftRegistries.ATTRIBUTES.stream().toList(), ItemUtils.BASE_MATERIAL_MODIFIER));
            resourcesToLoad.addAll(writeItemProviders(WandcraftRegistries.WAND_MODELS.stream().toList(), ItemUtils.BASE_MATERIAL_WAND));
            //resourcesToLoad.addAll(writeProviders(gson, List.of(new SpellbookItemTextureProvider()), ItemUtils.DISPLAY_MATERIAL_SPELLBOOK));
            resourcesToLoad.addAll(writeItemProviders(List.of(ITEM_PROVIDER_SPELLBOOK), ItemUtils.DISPLAY_MATERIAL_SPELLBOOK));
            resourcesToLoad.addAll(writeItemProviders(List.of(getSimpleProvider("blank_scroll")), ItemUtils.BASE_MATERIAL_BLANK_SCROLL));

            resourcesToLoad.addAll(writeItemProviders(WandcraftRegistries.HAT_TEXTURES.stream().toList(), true, false, ItemUtils.DISPLAY_MATERIAL_HAT));
        }

        resourcesToLoad.addAll(writeFontProviders(WandcraftRegistries.WAND_BACKGROUNDS.stream().toList()));
        generateSpaceFont();

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
        } else {
            plugin.getLogger().info("No resource pack changes detected.");
        }
        return false;
    }

    private static void generateSpaceFont() {
        Path fontFolder = WbsWandcraft.getInstance().getDataPath().resolve(getFontFolder(WANDCRAFT));

        Map<Character, Integer> advances = new HashMap<>();
        int range = 512;

        for (int i = -range; i < range; i++) {
            advances.put((char) (SPACE_CHAR_ZERO + i), i);
        }

        WbsFileUtil.writeJSONToFile(
                fontFolder.resolve("space").toFile(),
                new ResourcePackObjects.Font(
                        new ResourcePackObjects.FontProvider[] {
                                new ResourcePackObjects.SpaceFontProvider(advances)
                        }
                )
        );
    }

    private static <T extends FontElement> List<String> writeFontProviders(List<T> providers) {
        List<String> resourcesToLoad = new LinkedList<>();

        WbsWandcraft plugin = WbsWandcraft.getInstance();

        Multimap<String, T> fontElements = HashMultimap.create();
        for (T provider : providers) {
            boolean isValid = false;

            if (provider instanceof InventoryBackgroundFontElement backgroundElement) {
                isValid |= handleBackgroundFontElement(backgroundElement, resourcesToLoad);
            }

            if (isValid) {
                fontElements.put(provider.font(), provider);
            }
        }

        Path fontFolder = plugin.getDataPath().resolve(getFontFolder(WANDCRAFT));
        for (String font : fontElements.keySet()) {
            WbsFileUtil.writeJSONToFile(
                    fontFolder.resolve(font).toFile(),
                    new ResourcePackObjects.Font(
                            fontElements.get(font).stream()
                                    .map(FontElement::buildFontProvider)
                                    .toArray(ResourcePackObjects.FontProvider[]::new)
                    )
            );
        }

        return resourcesToLoad.stream().distinct().collect(Collectors.toCollection(LinkedList::new));
    }

    private static boolean handleBackgroundFontElement(InventoryBackgroundFontElement backgroundElement, List<String> resourcesToLoad) {
        WbsWandcraft plugin = WbsWandcraft.getInstance();

        boolean missingTextures = false;

        List<String> resources = new LinkedList<>();

        String imagePath = getTexturesFolder(WANDCRAFT) + backgroundElement.texturePath() + ".png";

        if (plugin.getResource(imagePath) == null) {
            if (plugin.getSettings().debugMode()) {
                plugin.getLogger().warning("Font resource missing! " + imagePath);
            }
            missingTextures = false;
            return missingTextures;
        }
        resources.add(imagePath);

        resourcesToLoad.addAll(resources);

        return !missingTextures;
    }

    private static <T extends ItemModelProvider> List<String> writeItemProviders(List<T> providers, Material ... materials) {
        return writeItemProviders(providers, false, false, materials);
    }
    private static <T extends ItemModelProvider> List<String> writeItemProviders(List<T> providers, boolean isBlock, boolean oversizedInGUI, Material ... materials) {
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

            if (provider instanceof ExistingItemProvider) {
                isValid = true;
            }

            if (isValid) {
                valid.add(provider);
            }
        }

        valid = valid.stream().distinct().collect(Collectors.toCollection(LinkedList::new));
        plugin.getLogger().info("Valid: " + valid.stream().map(ItemModelProvider::modelResourceLocation).collect(Collectors.joining(", ")));
        plugin.getLogger().info("Materials: " + Arrays.stream(materials).map(Material::getKey).map(Key::asMinimalString).collect(Collectors.joining(", ")));

        Path externalItemFolder = plugin.getDataPath().resolve(getItemsFolder(MINECRAFT));
        for (Material material : materials) {
            ItemDefinition selectorDef = createSelectorItemDefinition(
                    material,
                    valid.stream().toList(),
                    isBlock
            );

            selectorDef.setOversizedInGUI(oversizedInGUI);

            File materialItemDef = externalItemFolder.resolve(material.key().value()).toFile();
            plugin.getLogger().info("selectorDef " + materialItemDef.getName() + ": " + new Gson().toJson(selectorDef));
            WbsFileUtil.writeJSONToFile(
                    materialItemDef,
                    selectorDef
            );
        }

        return resourcesToLoad.stream().distinct().collect(Collectors.toCollection(LinkedList::new));
    }

    private static ItemDefinition createSelectorItemDefinition(Material baseMaterial, List<? extends ItemModelProvider> providers, boolean isBlock) {
        String fallbackType = "item";
        if (isBlock) {
            fallbackType = "block";
        }
        ResourcePackObjects.StaticModelReference fallback = new ResourcePackObjects.StaticModelReference("minecraft:" + fallbackType + "/" + baseMaterial.key().value(), null);

        ResourcePackObjects.SelectModelReference model = new ResourcePackObjects.SelectModelReference("custom_model_data", 0, fallback);

        for (ItemModelProvider provider : providers) {
            model.addCase(new ResourcePackObjects.ModelCase(
                    provider.modelKey(),
                    provider.buildBaseModel()
            ));
        }

        return new ItemDefinition(model);
    }

    private static boolean handleDynamicItemProvider(DynamicItemTextureProvider itemProvider, List<String> resourcesToLoad) {
        WbsWandcraft plugin = WbsWandcraft.getInstance();

        boolean missingTextures = false;

        List<String> resources = new LinkedList<>();

        for (TextureLayer texture : itemProvider.getTextures()) {
            String folder = ASSETS_FOLDER + texture.namespace() + "/textures/" + texture.folder() + "/";
            String imagePath = folder + texture.name() + ".png";

            if (plugin.getResource(imagePath) == null) {
                missingTextures = true;
                if (plugin.getSettings().debugMode()) {
                    plugin.getLogger().warning("Item resource missing! " + imagePath);
                }
                break;
            }

            resources.add(imagePath);
            if (texture.isAnimated()) {
                String metaPath = folder + texture.name() + ".png.mcmeta";
                resources.add(metaPath);
            }
        }

        if (!missingTextures) {
            itemProvider.getModelDefinitions().forEach((name, definition) -> {
                Path modelPath = plugin.getDataPath().resolve(getItemModelsFolder(WANDCRAFT));
                String subfolder = itemProvider.subfolder();
                if (subfolder != null) {
                    modelPath = modelPath.resolve(subfolder);
                }
                if (itemProvider instanceof WandBackground) {
                    plugin.getLogger().info("Wand background being written to " + modelPath + ": " + new Gson().toJson(definition));
                }
                WbsFileUtil.writeJSONToFile(
                        modelPath.resolve(name).toFile(),
                        definition
                );
            });

            resourcesToLoad.addAll(resources);
        }

        return !missingTextures;
    }

    private static boolean handleExternalItemProvider(ExternalItemProvider externalProvider, List<String> resourcesToLoad) {
        WbsWandcraft plugin = WbsWandcraft.getInstance();

        String folder = externalProvider.getModelType();
        String namespace = externalProvider.namespace();
        String modelsFolder = getModelsFolder(namespace, folder);

        String modelPath = modelsFolder + externalProvider.value() + ".json";
        resourcesToLoad.add(modelPath);

        for (String additionalModel : externalProvider.getAdditionalModels()) {
            resourcesToLoad.add(modelsFolder + additionalModel + ".json");
        }

        String texturesFolder = getTexturesFolder(namespace, folder);
        String texturePath = texturesFolder + externalProvider.value() + ".png";
        if (externalProvider.externalTexture()) {
            resourcesToLoad.add(texturePath);
        }

        for (String additionalTexture : externalProvider.getAdditionalTextures()) {
            resourcesToLoad.add(texturesFolder + additionalTexture + ".png");
        }

        // TODO: Only add resources to load if these are true
        return plugin.getResource(modelPath) != null && (!externalProvider.externalTexture() || plugin.getResource(texturePath) != null);
    }

    private static DynamicItemTextureProvider getSimpleProvider(String key) {
        return getSimpleProvider(WbsWandcraft.getKey(key), new TextureLayer(key));
    }
    private static DynamicItemTextureProvider getSimpleProvider(NamespacedKey key, TextureLayer layer) {
        return new DynamicItemTextureProvider() {
            @Override
            public @NotNull List<TextureLayer> getTextures() {
                return List.of(
                        layer
                );
            }

            @Override
            public @NotNull NamespacedKey getKey() {
                return key;
            }
        };
    }
    private static ExternalItemProvider getExternalProvider(NamespacedKey key) {
        return getExternalProvider(key, "item");
    }
    private static ExternalItemProvider getExternalProvider(NamespacedKey key, String modelType) {
        return new ExternalItemProvider() {
            @Override
            public @NotNull String getModelType() {
                return modelType;
            }

            @Override
            public @NotNull NamespacedKey getKey() {
                return key;
            }
        };
    }
}
