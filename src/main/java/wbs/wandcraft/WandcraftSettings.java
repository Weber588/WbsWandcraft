package wbs.wandcraft;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.plugin.WbsSettings;
import wbs.wandcraft.crafting.ArtificingConfig;
import wbs.wandcraft.generation.WandGenerator;
import wbs.wandcraft.resourcepack.ResourcePackBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WandcraftSettings extends WbsSettings {
    protected WandcraftSettings(WbsWandcraft plugin) {
        super(plugin);
    }
    private ArtificingConfig artificingConfig;
    public ArtificingConfig getArtificingConfig() {
        return artificingConfig;
    }

    private final Map<String, @Nullable NamespacedKey> itemModels = new HashMap<>();
    public void registerItemModel(String key, NamespacedKey modelKey) {
        itemModels.put(key, modelKey);
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

        ResourcePackBuilder.loadResourcePack(this, config);

        ConfigurationSection artificingTableSection = config.getConfigurationSection("artificing-table");
        if (artificingTableSection != null) {
            artificingConfig = new ArtificingConfig(artificingTableSection, this, "config.yml/artificing-table");
        }

        loadGenerators();
    }

    private void loadGenerators() {
        String path = "generators.yml";
        YamlConfiguration config = loadConfigSafely(genConfig(path));

        ConfigurationSection wandsConfig = config.getConfigurationSection("wands");

        if (wandsConfig != null) {
            for (String wandName : wandsConfig.getKeys(false)) {
                ConfigurationSection wandSection = wandsConfig.getConfigurationSection(wandName);

                WandGenerator generator = new WandGenerator(Objects.requireNonNull(wandSection), this, path + "/wands");
                WandcraftRegistries.WAND_GENERATORS.register(generator);
            }
        }
    }
}
