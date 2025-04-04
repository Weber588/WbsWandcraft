package wbs.wandcraft;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.configuration.WbsConfigReader;
import wbs.utils.util.plugin.WbsSettings;

import java.util.HashMap;
import java.util.Map;

public class WandcraftSettings extends WbsSettings {
    protected WandcraftSettings(WbsWandcraft plugin) {
        super(plugin);
    }

    private final Map<String, @Nullable NamespacedKey> itemModels = new HashMap<>();

    @Nullable
    public NamespacedKey getItemModel(String name) {
        return itemModels.get(name);
    }

    @Override
    public void reload() {
        YamlConfiguration config = loadDefaultConfig("config.yml");

        ConfigurationSection modelsSection = config.getConfigurationSection("item-models");
        if (modelsSection != null) {
            for (String key : modelsSection.getKeys(false)) {
                itemModels.put(key, WbsConfigReader.getNamespacedKey(modelsSection, key));
            }
        }
    }
}
