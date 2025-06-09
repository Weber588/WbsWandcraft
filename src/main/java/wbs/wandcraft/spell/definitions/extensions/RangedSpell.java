package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

public interface RangedSpell extends ISpellDefinition {
    SpellAttribute<Double> RANGE = new DoubleSpellAttribute("range", 20)
            .addSuggestions(10.0, 20.0, 50.0, 100.0)
            .setNumericFormatter(range -> range + " blocks");

    default void setupRanged() {
        addAttribute(RANGE);
    }
}
