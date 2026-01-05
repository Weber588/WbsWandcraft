package wbs.wandcraft;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
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
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.plugin.WbsSettings;
import wbs.wandcraft.crafting.ArtificingConfig;
import wbs.wandcraft.generation.AttributeModifierGenerator;
import wbs.wandcraft.generation.SpellInstanceGenerator;
import wbs.wandcraft.generation.WandGenerator;
import wbs.wandcraft.learning.LearningMethod;
import wbs.wandcraft.learning.LearningTrigger;
import wbs.wandcraft.learning.LearningMethodType;
import wbs.wandcraft.resourcepack.ResourcePackBuilder;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.util.ItemUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
        loadLearning();
        loadCredits();
    }

    private static final Multimap<SpellDefinition, LearningMethod> LEARNING_MAP = HashMultimap.create();

    public Multimap<SpellDefinition, LearningMethod> getLearningMap() {
        return HashMultimap.create(LEARNING_MAP);
    }

    private void loadLearning() {
        LEARNING_MAP.clear();

        String path = "learning.yml";
        YamlConfiguration config = loadConfigSafely(genConfig(path));

        ConfigurationSection spellsSection = config.getConfigurationSection("spells");
        if (spellsSection != null) {
            Set<String> keys = spellsSection.getKeys(false);
            String spellDirectory = path + "/spells";

            for (String spellKeyString : keys) {
                NamespacedKey spellKey = NamespacedKey.fromString(spellKeyString, WbsWandcraft.getInstance());
                if (spellKey == null) {
                    logError("Failed to parse key: " + spellKeyString, spellDirectory);
                    continue;
                }

                SpellDefinition spellDefinition = WandcraftRegistries.SPELLS.get(spellKey);
                if (spellDefinition == null) {
                    logError("Unrecognised spell: " + spellKeyString + " (" + spellKey.asString() + ")", spellDirectory);
                    continue;
                }

                ConfigurationSection learningSection = spellsSection.getConfigurationSection(spellKeyString);
                if (learningSection == null) {
                    logError("Must be a section: " + spellKeyString, spellDirectory);
                    continue;
                }

                String learningDirectory = spellDirectory + "/" + spellKeyString;
                for (String learningKeyString : learningSection.getKeys(false)) {
                    NamespacedKey learningKey = NamespacedKey.fromString(learningKeyString, WbsWandcraft.getInstance());
                    if (learningKey == null) {
                        logError("Failed to parse key: " + learningKeyString, learningDirectory);
                        continue;
                    }

                    LearningMethodType<?> type = WandcraftRegistries.LEARNING_PROVIDERS.get(learningKey);
                    if (type == null) {
                        logError("Unrecognised learning type: " + learningKeyString + " (" + learningKey.asString() + ")", learningDirectory);
                        continue;
                    }

                    try {
                        LearningTrigger<?> criteria = type.construct(learningSection, learningKeyString, learningDirectory + "/" + learningKeyString);

                        LEARNING_MAP.put(spellDefinition, criteria);
                    } catch (InvalidConfigurationException ex) {
                        logError(ex.getMessage(), ex.getDirectory());
                    }
                }
            }
        }
    }

    // TODO: Make these configurable
    private void loadRecipes() {
        ShapelessRecipe spellbook = new ShapelessRecipe(WbsWandcraft.getKey("spellbook"), ItemUtils.buildSpellbook());

        spellbook.addIngredient(ItemStack.of(Material.FEATHER));
        spellbook.addIngredient(ItemStack.of(Material.BOOK));
        spellbook.addIngredient(ItemStack.of(Material.AMETHYST_SHARD));

        Bukkit.removeRecipe(spellbook.getKey());
        Bukkit.addRecipe(spellbook);

        ShapelessRecipe blankScroll = new ShapelessRecipe(WbsWandcraft.getKey("blank_scroll"), ItemUtils.buildBlankScroll());

        int paperInRecipe = 6;
        for (int i = 0; i < paperInRecipe; i++) {
            blankScroll.addIngredient(ItemStack.of(Material.PAPER));
        }

        Bukkit.removeRecipe(blankScroll.getKey());
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

        Bukkit.removeRecipe(artificingTable.getKey());
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

        ConfigurationSection spellsConfig = config.getConfigurationSection("spells");

        if (spellsConfig != null) {
            for (String spellName : spellsConfig.getKeys(false)) {
                ConfigurationSection spellsSection = spellsConfig.getConfigurationSection(spellName);

                SpellInstanceGenerator generator = new SpellInstanceGenerator(
                        Objects.requireNonNull(spellsSection),
                        this,
                        path + "/spells"
                );
                WandcraftRegistries.SPELL_GENERATORS.register(generator);
            }
        }

        ConfigurationSection modifierConfig = config.getConfigurationSection("modifiers");

        if (modifierConfig != null) {
            for (String attributeName : modifierConfig.getKeys(false)) {
                ConfigurationSection modifierSection = modifierConfig.getConfigurationSection(attributeName);

                AttributeModifierGenerator<?> generator = AttributeModifierGenerator.fromConfig(
                        Objects.requireNonNull(modifierSection),
                        this,
                        path + "/modifiers",
                        AttributeModifierType.SET
                );
                if (generator != null) {
                    WandcraftRegistries.MODIFIER_GENERATORS.register(generator);
                } else {
                    logError("Attribute modifier was null!", path + "/modifiers");
                }
            }
        }
    }
}
