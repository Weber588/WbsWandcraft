package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.AbstractSpellDefinition;

public interface RangedSpell extends AbstractSpellDefinition {
    SpellAttribute<Double> RANGE = new DoubleSpellAttribute("range", 0.001, 20)
            .addSuggestions(10.0, 20.0, 50.0, 100.0);

    default void setupRanged() {
        addAttribute(RANGE);
    }
}
