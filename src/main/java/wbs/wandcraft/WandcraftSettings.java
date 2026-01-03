package wbs.wandcraft;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.plugin.WbsSettings;
import wbs.wandcraft.crafting.ArtificingConfig;
import wbs.wandcraft.generation.WandGenerator;
import wbs.wandcraft.resourcepack.ResourcePackBuilder;
import wbs.wandcraft.util.ItemUtils;

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

        loadRecipes();
        loadGenerators();
        loadCredits();
    }

    // TODO: Make these configurable
    private void loadRecipes() {
        ShapelessRecipe spellbook = new ShapelessRecipe(WbsWandcraft.getKey("spellbook"), ItemUtils.buildSpellbook());

        spellbook.addIngredient(ItemStack.of(Material.FEATHER));
        spellbook.addIngredient(ItemStack.of(Material.BOOK));
        spellbook.addIngredient(ItemStack.of(Material.AMETHYST_SHARD));

        Bukkit.addRecipe(spellbook);

        ShapelessRecipe blankScroll = new ShapelessRecipe(WbsWandcraft.getKey("blank_scroll"), ItemUtils.buildBlankScroll());

        int paperInRecipe = 6;
        for (int i = 0; i < paperInRecipe; i++) {
            blankScroll.addIngredient(ItemStack.of(Material.PAPER));
        }

        Bukkit.addRecipe(blankScroll);

        ShapedRecipe artificingTable = new ShapedRecipe(WbsWandcraft.getKey("artificing_table"), getArtificingConfig().getItem());

        artificingTable.shape(
                "ddd",
                "ebe",
                "bbb"
        );
        artificingTable.setIngredient('d', Material.DEEPSLATE);
        artificingTable.setIngredient('e', Material.ECHO_SHARD);
        artificingTable.setIngredient('b', Material.BLACKSTONE);

        Bukkit.addRecipe(artificingTable);
    }

    private void loadCredits() {
        saveResource("credits.txt", true);
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
