package wbs.wandcraft.generation;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsCollectionUtil;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WandcraftSettings;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.util.ItemUtils;

import java.util.*;

@NullMarked
public class SpellInstanceGenerator implements Keyed, ItemGenerator {
    private final List<SpellDefinition> definitions = new LinkedList<>(WandcraftRegistries.SPELLS.values());
    private final List<AttributeModifierGenerator<?>> modifierGenerators = new LinkedList<>();
    private final NamespacedKey key;

    public SpellInstanceGenerator(ConfigurationSection section, WandcraftSettings settings, String directory) {
        readSpellDefinitions(section, settings, directory + "/spells");

        readAttributes(section, settings, directory + "/attributes");

        NamespacedKey tempKey = null;
        String keyString = section.getString("key");
        if (keyString != null) {
            NamespacedKey checkKey = NamespacedKey.fromString(keyString, WbsWandcraft.getInstance());
            if (checkKey == null) {
                settings.logError("Invalid key \"" + keyString + "\"", directory + "/key");
                tempKey = WbsWandcraft.getKey("anonymous");
            }
        }
        if (tempKey == null) {
            tempKey = NamespacedKey.fromString(section.getName(), WbsWandcraft.getInstance());
        }
        if (tempKey == null) {
            tempKey = WbsWandcraft.getKey("anonymous");
        }
        key = tempKey;
    }

    private void readSpellDefinitions(ConfigurationSection section, WandcraftSettings settings, String directory) {
        List<String> spellStrings = section.getStringList("spells");

        List<SpellDefinition> definitions = new LinkedList<>();

        for (String spellString : spellStrings) {
            NamespacedKey key;
            try {
                key = NamespacedKey.fromString(spellString, WbsWandcraft.getInstance());
                if (key == null) {
                    throw new IllegalArgumentException();
                }
            } catch (IllegalArgumentException ex) {
                settings.logError("Invalid spell definition key \"" + spellString + "\".", directory + "/" + spellString);
                continue;
            }

            SpellDefinition spellDef = WandcraftRegistries.SPELLS.get(key);
            if (spellDef == null) {
                settings.logError("Spell definition not found for key \"" + key.asString() + "\".", directory + "/" + spellString);
            } else {
                definitions.add(spellDef);
            }
        }

        if (!definitions.isEmpty()) {
            setSpellDefinitions(definitions);
        }
    }

    private void readAttributes(ConfigurationSection section, WandcraftSettings settings, String directory) {
        ConfigurationSection attributesSection = section.getConfigurationSection("attributes");

        if (attributesSection != null) {
            Set<String> keys = attributesSection.getKeys(false);

            for (String key : keys) {
                try {
                    AttributeModifierGenerator<?> modifier = AttributeModifierGenerator.fromConfig(
                            Objects.requireNonNull(attributesSection.getConfigurationSection(key)),
                            settings,
                            directory + "/" + key
                    );

                    modifierGenerators.add(modifier);
                } catch (InvalidConfigurationException ex) {
                    settings.logError(ex.getMessage(), ex.getDirectory());
                }
            }
        }
    }

    public SpellInstance generate() {
        SpellInstance instance = new SpellInstance(WbsCollectionUtil.getRandom(definitions));

        for (AttributeModifierGenerator<?> generator : modifierGenerators) {
            SpellAttributeModifier<?, ?> modifier = generator.generate();

            // Only add the attribute if it's valid for the chosen spell
            if (instance.getAttribute(modifier.attribute()) != null) {
                instance.applyModifier(modifier);
            }
        }

        return instance;
    }

    public SpellInstanceGenerator setSpellDefinitions(SpellDefinition ... definitions) {
        return setSpellDefinitions(Arrays.asList(definitions));
    }
    public SpellInstanceGenerator setSpellDefinitions(List<SpellDefinition> definitions) {
        this.definitions.clear();

        if (definitions.isEmpty()) {
            this.definitions.addAll(WandcraftRegistries.SPELLS.values());
        } else {
            this.definitions.addAll(definitions);
        }

        return this;
    }

    public List<SpellDefinition> getSpells() {
        return new LinkedList<>(definitions);
    }

    public SpellInstanceGenerator addAttributeGenerator(AttributeModifierGenerator<?> generator) {
        modifierGenerators.add(generator);
        return this;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public ItemStack generateItem() {
        return ItemUtils.buildSpell(generate());
    }
}
