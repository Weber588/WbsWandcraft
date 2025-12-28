package wbs.wandcraft.resourcepack;

import com.google.gson.Gson;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import wbs.utils.util.WbsFileUtil;
import wbs.utils.util.configuration.WbsConfigReader;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WandcraftSettings;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.modifier.ModifierTexture;
import wbs.wandcraft.spellbook.Spellbook;
import wbs.wandcraft.util.ItemUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static wbs.wandcraft.resourcepack.ResourcePackObjects.ItemSelectorDefinition;

public class ResourcePackBuilder {

    public static final String ITEMS_FOLDER = "resourcepack/assets/minecraft/items/";
    public static final String ITEM_MODELS_PATH = "resourcepack/assets/" + WbsWandcraft.getInstance().namespace() + "/models/item/";
    public static final String TEXTURES_PATH = "resourcepack/assets/" + WbsWandcraft.getInstance().namespace() + "/textures/item/";

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
            resourcesToLoad.addAll(writeProviders(gson, Arrays.stream(ModifierTexture.values()).toList(), ItemUtils.BASE_MATERIAL_MODIFIER));
            resourcesToLoad.addAll(writeProviders(gson, WandcraftRegistries.WAND_TEXTURES.stream().toList(), ItemUtils.BASE_MATERIAL_WAND));
            resourcesToLoad.addAll(writeProviders(gson, List.of(new Spellbook()), ItemUtils.DISPLAY_MATERIAL_SPELLBOOK));

            resourcesToLoad.add(ITEM_MODELS_PATH + "blank_scroll.json");
            resourcesToLoad.add(TEXTURES_PATH + "blank_scroll.png");

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

    private static <T extends TextureProvider> Set<String> writeProviders(Gson gson, List<T> providers, Material baseMaterial) {
        Set<String> resourcesToLoad = new HashSet<>();

        WbsWandcraft plugin = WbsWandcraft.getInstance();

        writeJSONToFile(
                plugin.getDataPath().resolve(ResourcePackBuilder.ITEMS_FOLDER),
                baseMaterial.key().value(),
                gson,
                new ItemSelectorDefinition(
                        baseMaterial,
                        providers
                )
        );

        providers.forEach(provider -> {
            provider.getModelDefinitions().forEach((name, definition) -> {
                writeJSONToFile(
                        plugin.getDataPath().resolve(ResourcePackBuilder.ITEM_MODELS_PATH),
                        name,
                        gson,
                        definition
                );
            });

            for (TextureLayer texture : provider.getTextures()) {
                resourcesToLoad.add(TEXTURES_PATH + texture.name() + ".png");
                if (texture.isAnimated()) {
                    resourcesToLoad.add(TEXTURES_PATH + texture.name() + ".png.mcmeta");
                }
            }
        });

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
}
