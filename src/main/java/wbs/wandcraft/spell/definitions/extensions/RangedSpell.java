package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

public interface RangedSpell extends ISpellDefinition {
    SpellAttribute<Double> RANGE = new DoubleSpellAttribute("range", 40)
            .addSuggestions(10.0, 20.0, 50.0, 100.0)
            .setNumericFormatter(value -> value + " blocks")
            .setShowAttribute(value -> value > 0);

    default void setupRanged() {
        addAttribute(RANGE);
    }
}
