package wbs.wandcraft.resourcepack;

import com.google.gson.Gson;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import wbs.utils.util.WbsFileUtil;
import wbs.utils.util.configuration.WbsConfigReader;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WandcraftSettings;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.modifier.ModifierTexture;
import wbs.wandcraft.util.ItemUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static wbs.wandcraft.resourcepack.ResourcePackObjects.*;

public class ResourcePackBuilder {
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

            String itemsFolder = "resourcepack/assets/minecraft/items/";
            String itemModelsPath = "resourcepack/assets/" + namespace + "/models/item/";
            String texturesPath = "resourcepack/assets/" + namespace + "/textures/item/";

            Gson gson = new Gson();

            writeJSONToFile(plugin.getDataPath().resolve(itemsFolder), ItemUtils.BASE_MATERIAL_SPELL.getKey(), gson, new ItemSelectorDefinition(
                    ItemUtils.BASE_MATERIAL_SPELL,
                    WandcraftRegistries.SPELLS.stream().toList())
            );

            writeJSONToFile(plugin.getDataPath().resolve(itemsFolder), ItemUtils.BASE_MATERIAL_MODIFIER.getKey(), gson,
                    new ItemSelectorDefinition(
                            ItemUtils.BASE_MATERIAL_MODIFIER,
                            Arrays.stream(ModifierTexture.values()).toList(),
                            List.of(new ModelTint())
                    )
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
                        key.getNamespace() + ":item/" + definition.getTexture()
                ));
                resourcesToLoad.add(texturesPath + definition.getTexture() + ".png");
            });

            resourcesToLoad.add(WbsWandcraft.getKey("item/modifier_overlay").asString());
            resourcesToLoad.add(texturesPath + "modifier_overlay.png");
            Arrays.stream(ModifierTexture.values()).forEach(texture -> {
                NamespacedKey key = texture.getKey();

                String texturePath = key.getNamespace() + ":item/modifier_overlay";
                String baseTexture = key.getNamespace() + ":item/" + texture.getTexture();
                writeJSONToFile(plugin.getDataPath().resolve(itemModelsPath), key, gson, new ModelDefinition(
                        "minecraft:item/handheld",
                        texturePath,
                        baseTexture
                ));
                resourcesToLoad.add(texturesPath + texture.getTexture() + ".png");
            });

            WandcraftRegistries.WAND_TEXTURES.stream().forEach(texture -> {
                NamespacedKey key = texture.getKey();

                String texturePath = key.getNamespace() + ":item/" + texture.getTexture();
                String baseTexture = key.getNamespace() + ":item/" + texture.getBaseTexture();
                writeJSONToFile(plugin.getDataPath().resolve(itemModelsPath), key, gson, new ModelDefinition(
                        "minecraft:item/handheld",
                        texturePath,
                        baseTexture
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
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
}
