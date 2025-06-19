package wbs.wandcraft.resourcepack;

import com.google.gson.Gson;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import wbs.utils.util.WbsFileUtil;
import wbs.utils.util.configuration.WbsConfigReader;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WandcraftSettings;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.modifier.ModifierScope;
import wbs.wandcraft.util.ItemUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static wbs.wandcraft.resourcepack.ResourcePackObjects.*;

public class ResourcePackBuilder {
    public static void loadResourcePack(WandcraftSettings settings, YamlConfiguration config) {
        WbsWandcraft plugin = WbsWandcraft.getInstance();

        ConfigurationSection modelsSection = config.getConfigurationSection("item-models");
        if (modelsSection != null) {
            ConfigurationSection vanillaModelsSection = config.getConfigurationSection("vanilla-models");
            if (vanillaModelsSection != null) {
                for (String key : vanillaModelsSection.getKeys(false)) {
                    settings.registerItemModel(key, WbsConfigReader.getNamespacedKey(vanillaModelsSection, key));
                }
            }

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
                        key.getNamespace() + ":item/" + definition.getTexture()
                ));
                resourcesToLoad.add(texturesPath + definition.getTexture() + ".png");
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
