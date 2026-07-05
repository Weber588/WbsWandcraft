package wbs.wandcraft;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.plugin.WbsSettings;
import wbs.wandcraft.crafting.ArtificingConfig;
import wbs.wandcraft.generation.AttributeModifierGenerator;
import wbs.wandcraft.generation.SpellInstanceGenerator;
import wbs.wandcraft.generation.WandGenerator;
import wbs.wandcraft.learning.*;
import wbs.wandcraft.resourcepack.ResourcePackBuilder;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.util.ItemUtils;

import java.util.*;

public class WandcraftSettings extends WbsSettings {

    public static final String SPELL_CONFIG_COST = "echo-shard-cost";
    public static final String SPELL_CONFIG_ATTRIBUTES = "attributes";
    private boolean debugMode = false;
    private final Map<@NotNull EntityType, @NotNull Map<ItemStack, @NotNull Double>> customDrops = new HashMap<>();

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

    @NullMarked
    public Map<EntityType, Map<ItemStack, Double>> getCustomDrops() {
        return this.customDrops;
    }

    @Override
    public void reload() {
        String directory = "config.yml";
        YamlConfiguration config = this.loadDefaultConfig(directory);

        ConfigurationSection artificingTableSection = config.getConfigurationSection("artificing-table");

        if (artificingTableSection != null) {
            this.artificingConfig = new ArtificingConfig(artificingTableSection, this, directory + "/artificing-table");
        }

        ResourcePackBuilder.loadResourcePack(this, config);

        this.debugMode = config.getBoolean("debug", false);

        this.loadSpellConfigs();
        this.loadRecipes();
        this.loadGenerators();
        this.loadLearning();
        this.loadGeneration();
        this.loadCredits();
        this.loadCustomDrops(config, directory);
    }

    private void loadCustomDrops(YamlConfiguration config, String directory) {
        ConfigurationSection customDropsSection = config.getConfigurationSection("custom-drops");
        if (customDropsSection != null) {
            String dropDirectory = directory + "/custom-drops";

            for(String entityTypeKey : customDropsSection.getKeys(false)) {
                EntityType type = WbsEnums.getEnumFromString(EntityType.class, entityTypeKey);
                String entityDropsDir = dropDirectory + "/" + entityTypeKey;
                if (type == null) {
                    this.logError("Invalid entity type \"%s\"".formatted(entityTypeKey), entityDropsDir);
                    continue;
                }

                ConfigurationSection entityDropsSection = customDropsSection.getConfigurationSection(entityTypeKey);
                if (entityDropsSection == null) {
                    this.logError("Entity type must have a section: %s".formatted(entityTypeKey), entityDropsDir);
                    continue;
                }

                Set<String> itemTypeKeys = entityDropsSection.getKeys(false);
                Map<ItemStack, Double> itemChances = new HashMap<>();

                for(String itemTypeKey : itemTypeKeys) {
                    Object chanceObj = entityDropsSection.get(itemTypeKey);
                    if (!(chanceObj instanceof Number number)) {
                        this.logError("Invalid number: %s".formatted(chanceObj), entityDropsDir);
                        continue;
                    }

                    double chance = number.doubleValue();

                    try {
                        ItemStack stack = Bukkit.getItemFactory().createItemStack(itemTypeKey);
                        itemChances.put(stack, chance);
                    } catch (IllegalArgumentException var19) {
                        this.logError("Invalid item stack: %s".formatted(itemTypeKey), entityDropsDir);
                    }
                }

                if (!itemChances.isEmpty()) {
                    this.customDrops.put(type, itemChances);
                }
            }
        }
    }

    public boolean debugMode() {
        return debugMode;
    }

    private void loadSpellConfigs() {
        Collection<SpellDefinition> definitions = WandcraftRegistries.SPELLS.values();

        String path = "spells.yml";
        YamlConfiguration config = loadConfigSafely(genConfig(path));

        boolean requiresUpdate = false;
        for (SpellDefinition definition : definitions) {
            String sectionKey = definition.key().asMinimalString();
            ConfigurationSection section = config.getConfigurationSection(sectionKey);

            if (section == null) {
                requiresUpdate = true;
                section = config.createSection(sectionKey);
                section.set(SPELL_CONFIG_COST, -1); // Fallback to calculation
            }

            int cost = section.getInt(SPELL_CONFIG_COST, -1);
            definition.setEchoShardCost(cost);

            ConfigurationSection attributesSection = section.getConfigurationSection(SPELL_CONFIG_ATTRIBUTES);
            if (attributesSection == null) {
                requiresUpdate = true;
                attributesSection = section.createSection(SPELL_CONFIG_ATTRIBUTES);
            }

            Set<SpellAttributeInstance<?>> attributeInstances = new HashSet<>(definition.getAttributeInstances());
            for (SpellAttributeInstance<?> attributeInstance : attributeInstances) {
                SpellAttribute<?> attribute = attributeInstance.attribute();
                String attributeKey = attribute.key().asMinimalString();

                if (attributesSection.isString(attributeKey)) {
                    String asString = attributesSection.getString(attributeKey, String.valueOf(attributesSection.get(attributeKey)));

                    SpellAttributeInstance<?> parsed = attribute.getParsedInstance(asString);
                    definition.setAttribute(parsed);
                } else {
                    requiresUpdate = true;
                    if (attributeInstance.value() instanceof Number number) {
                        attributesSection.set(attributeKey, number);
                    } else {
                        attributesSection.set(attributeKey, attributeInstance.rawStringValue());
                        if (attributeInstance.value() instanceof Particle) {
                            // TODO: Allow attributes to dynamically decide what to put here
                            attributesSection.setComments(attributeKey, List.of(
                                    "Warning: Not all particles are supported for all spells.",
                                    "Be cautious when changing to or from particles that require data."
                            ));
                        }
                    }
                }
            }
        }

        if (requiresUpdate) {
            saveYamlData(config, path, "spell", ignored -> {});
        }
    }

    private final Multimap<SpellDefinition, LearningMethod> learningMap = HashMultimap.create();

    public Multimap<SpellDefinition, LearningMethod> getLearningMap() {
        return HashMultimap.create(learningMap);
    }

    private void loadLearning() {
        learningMap.clear();

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
                        LearningMethod criteria = type.construct(learningSection, learningKeyString, learningDirectory + "/" + learningKeyString);

                        learningMap.put(spellDefinition, criteria);
                    } catch (InvalidConfigurationException ex) {
                        logError(ex.getMessage(), ex.getDirectory());
                    }
                }
            }
        }
    }

    private final List<RegistrableLearningMethod> generationMethods = new LinkedList<>();
    public List<RegistrableLearningMethod> getGenerationMethods() {
        return new LinkedList<>(generationMethods);
    }

    private void loadGeneration() {
        generationMethods.clear();

        String path = "generation.yml";
        YamlConfiguration config = loadConfigSafely(genConfig(path));

        ConfigurationSection tradingSection = config.getConfigurationSection("trading");
        if (tradingSection != null) {
            String tradingDirectory = path + "/trading";
            for (String methodKey : tradingSection.getKeys(false)) {
                String methodDirectory = tradingDirectory + "/" + methodKey;

                try {
                    TradingMethod tradingMethod = new TradingMethod(tradingSection, methodKey, methodDirectory);
                    generationMethods.add(tradingMethod);
                } catch (InvalidConfigurationException ex) {
                    logError(ex.getMessage(), ex.getDirectory());
                }
            }
        }
        ConfigurationSection barteringSection = config.getConfigurationSection("bartering");
        if (barteringSection != null) {
            String barteringDirectory = path + "/bartering";
            for (String methodKey : barteringSection.getKeys(false)) {
                String methodDirectory = barteringDirectory + "/" + methodKey;

                try {
                    BarteringMethod barteringMethod = new BarteringMethod(barteringSection, methodKey, methodDirectory);
                    generationMethods.add(barteringMethod);
                } catch (InvalidConfigurationException ex) {
                    logError(ex.getMessage(), ex.getDirectory());
                }
            }
        }
        ConfigurationSection lootTablesSection = config.getConfigurationSection("loot-tables");
        if (lootTablesSection != null) {
            String lootTablesDirectory = path + "/loot-tables";
            for (String methodKey : lootTablesSection.getKeys(false)) {
                String methodDirectory = lootTablesDirectory + "/" + methodKey;

                try {
                    LootTableMethod lootTableMethod = new LootTableMethod(lootTablesSection, methodKey, methodDirectory);
                    generationMethods.add(lootTableMethod);
                } catch (InvalidConfigurationException ex) {
                    logError(ex.getMessage(), ex.getDirectory());
                }
            }
        }
    }

    // TODO: Make these configurable
    private void loadRecipes() {
        ShapelessRecipe spellbook = new ShapelessRecipe(WbsWandcraft.getKey("spellbook"), ItemUtils.buildSpellbook());

        spellbook.addIngredient(ItemStack.of(Material.FEATHER));
        spellbook.addIngredient(ItemStack.of(Material.BOOK));
        spellbook.addIngredient(ItemStack.of(Material.DRAGON_BREATH));

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
                "dsd",
                "ebe",
                "bbb"
        );
        artificingTable.setIngredient('d', Material.POLISHED_DEEPSLATE);
        artificingTable.setIngredient('s', Material.NETHER_STAR);
        artificingTable.setIngredient('e', Material.ECHO_SHARD);
        artificingTable.setIngredient('b', Material.POLISHED_BLACKSTONE);

        Bukkit.removeRecipe(artificingTable.getKey());
        Bukkit.addRecipe(artificingTable);
    }

    private void loadCredits() {
        saveResource("credits.txt", true);
    }

    private void loadGenerators() {
        String path = "item-generators.yml";
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

                try {
                    AttributeModifierGenerator<?> generator = AttributeModifierGenerator.fromConfig(
                            Objects.requireNonNull(modifierSection),
                            this,
                            path + "/modifiers"
                    );

                    WandcraftRegistries.MODIFIER_GENERATORS.register(generator);
                } catch (InvalidConfigurationException ex) {
                    logError(ex.getMessage(), ex.getDirectory());
                }
            }
        }
    }
}
