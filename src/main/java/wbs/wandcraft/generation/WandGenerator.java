package wbs.wandcraft.generation;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.WbsCollectionUtil;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WandcraftSettings;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.util.ItemUtils;
import wbs.wandcraft.wand.Wand;
import wbs.wandcraft.wand.types.WandType;
import wbs.wandcraft.wand.types.WizardryWand;
import wbs.wandcraft.wand.types.WizardryWandHolder;

import java.util.*;

public class WandGenerator implements Keyed {
    private final List<WandType<?>> types = new LinkedList<>(WandcraftRegistries.WAND_TYPES.values());

    private final int minAttributes;
    private final int maxAttributes;
    private final List<AttributeInstanceGenerator<?>> attributeGenerators = new LinkedList<>();
    private final int minModifiers;
    private final int maxModifiers;
    private final List<AttributeModifierGenerator<?>> modifierGenerators = new LinkedList<>();

    private final int minSpells;
    private final int maxSpells;

    private final List<SpellInstanceGenerator> spellGenerators = new LinkedList<>();
    private final @NotNull NamespacedKey key;

    public WandGenerator(@NotNull NamespacedKey key,
                         int minAttributes,
                         int maxAttributes,
                         int minModifiers,
                         int maxModifiers,
                         int minSpells,
                         int maxSpells
    ) {
        this.key = key;
        this.minAttributes = minAttributes;
        this.maxAttributes = maxAttributes;
        this.minModifiers = minModifiers;
        this.maxModifiers = maxModifiers;
        this.minSpells = minSpells;
        this.maxSpells = maxSpells;
    }

    public WandGenerator(@NotNull ConfigurationSection section, WandcraftSettings settings, String directory) {
        String name = section.getName();

        key = WbsWandcraft.getKey(name);

        ConfigurationSection spellsSection = section.getConfigurationSection("spells");
        if (spellsSection == null) {
            minSpells = 0;
            maxSpells = 0;
        } else {
            minSpells = spellsSection.getInt("min", 1);
            maxSpells = spellsSection.getInt("max", 1);

            ConfigurationSection generatorsSection = spellsSection.getConfigurationSection("generators");
            String spellGenDirectory = directory + "/spells/generators";
            if (generatorsSection == null) {
                settings.logError("\"generators\" is a required section when adding spells.", spellGenDirectory);
            } else {
                for (String spellString : generatorsSection.getKeys(false)) {
                    ConfigurationSection generatorSection = Objects.requireNonNull(generatorsSection.getConfigurationSection(spellString));

                    spellGenerators.add(new SpellInstanceGenerator(
                            generatorSection,
                            settings,
                            spellGenDirectory + "/" + spellString
                    ));
                }
            }
        }

        ConfigurationSection attributesSection = section.getConfigurationSection("attributes");
        if (attributesSection == null) {
            minAttributes = 0;
            maxAttributes = 0;
        } else {
            minAttributes = attributesSection.getInt("min", 1);
            maxAttributes = attributesSection.getInt("max", 1);

            ConfigurationSection generatorsSection = attributesSection.getConfigurationSection("generators");
            String attrGenDirectory = directory + "/attributes/generators";
            if (generatorsSection == null) {
                settings.logError("\"generators\" is a required section when adding attributes.", attrGenDirectory);
            } else {
                for (String attributeString : generatorsSection.getKeys(false)) {
                    NamespacedKey attributeKey = NamespacedKey.fromString(attributeString, WbsWandcraft.getInstance());

                    SpellAttribute<?> attribute = WandcraftRegistries.ATTRIBUTES.get(attributeKey);
                    if (attribute == null) {
                        settings.logError("Invalid attribute key \"" + attributeString + "\".", attrGenDirectory + "/" + attributeString);
                    } else {
                        ConfigurationSection generatorSection = generatorsSection.getConfigurationSection(attributeString);

                        attributeGenerators.add(new AttributeInstanceGenerator<>(
                                attribute,
                                Objects.requireNonNull(generatorSection),
                                settings,
                                attrGenDirectory + "/" + attributeString
                        ));
                    }
                }
            }
        }

        ConfigurationSection modifiersSection = section.getConfigurationSection("modifiers");
        if (modifiersSection == null) {
            minModifiers = 0;
            maxModifiers = 0;
        } else {
            minModifiers = modifiersSection.getInt("min", 1);
            maxModifiers = modifiersSection.getInt("max", 1);

            ConfigurationSection generatorsSection = modifiersSection.getConfigurationSection("generators");
            String modGenDirectory = directory + "/modifiers/generators";
            if (generatorsSection == null) {
                settings.logError("\"generators\" is a required section when adding modifiers.", modGenDirectory);
            } else {
                for (String attributeString : generatorsSection.getKeys(false)) {
                    AttributeModifierGenerator<?> modifier = AttributeModifierGenerator.fromConfig(
                            Objects.requireNonNull(generatorsSection.getConfigurationSection(attributeString)),
                            settings,
                            modGenDirectory + "/" + attributeString,
                            AttributeModifierType.ADD
                    );

                    if (modifier != null) {
                        modifierGenerators.add(modifier);
                    }
                }
            }
        }
    }

    public ItemStack get() {
        WandType<?> type = WbsCollectionUtil.getRandom(types);

        ItemStack wandItem = ItemUtils.buildWand(type);

        Wand wand = Objects.requireNonNull(Wand.getIfValid(wandItem));

        if (!attributeGenerators.isEmpty()) {
            int attributes = new Random().nextInt(minAttributes, maxAttributes + 1);
            for (int i = 0; i < attributes; i++) {
                AttributeInstanceGenerator<?> attributeGenerator = WbsCollectionUtil.getRandom(attributeGenerators);

                wand.setAttribute(attributeGenerator.get());
            }
        }

        if (!modifierGenerators.isEmpty()) {
            int modifiers = new Random().nextInt(minModifiers, maxModifiers + 1);
            for (int i = 0; i < modifiers; i++) {
                AttributeModifierGenerator<?> modifierGenerator = WbsCollectionUtil.getRandom(modifierGenerators);

                wand.setModifier(modifierGenerator.get());
            }
        }

        if (!spellGenerators.isEmpty()) {
            // TODO: Implement for other wand types
            if (wand instanceof WizardryWand wizardryWand) {
                WizardryWandHolder wandHolder = wizardryWand.getMenu(wandItem);
                Inventory inventory = wandHolder.getInventory();

                int spells = new Random().nextInt(minSpells, maxSpells + 1);
                for (int i = 0; i < spells; i++) {
                    SpellInstanceGenerator spellGenerator = WbsCollectionUtil.getRandom(spellGenerators);
                    SpellInstance instance = spellGenerator.get();

                    ItemStack instanceItem = ItemUtils.buildSpell(instance);

                    inventory.addItem(instanceItem);
                }

                List<@Nullable ItemStack> contentsList = Arrays.asList(inventory.getContents());
                Collections.shuffle(contentsList);
                inventory.setContents(contentsList.toArray(ItemStack[]::new));

                wandHolder.save();
            }
        }

        wand.toItem(wandItem);

        return wandItem;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public WandGenerator setTypes(WandType<?> ... types) {
        return setTypes(Arrays.asList(types));
    }
    public WandGenerator setTypes(List<WandType<?>> types) {
        this.types.clear();

        if (types.isEmpty()) {
            this.types.addAll(WandcraftRegistries.WAND_TYPES.values());
        } else {
            this.types.addAll(types);
        }

        return this;
    }

    public WandGenerator addSpellGenerator(SpellInstanceGenerator generator) {
        spellGenerators.add(generator);
        return this;
    }

    public WandGenerator addAttributeGenerator(AttributeInstanceGenerator<?> generator) {
        attributeGenerators.add(generator);
        return this;
    }

    public WandGenerator addModifierGenerator(AttributeModifierGenerator<?> generator) {
        modifierGenerators.add(generator);
        return this;
    }
}
