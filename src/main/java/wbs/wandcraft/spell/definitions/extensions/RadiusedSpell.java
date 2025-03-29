package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.AbstractSpellDefinition;

public interface RadiusedSpell extends AbstractSpellDefinition {
    SpellAttribute<Double> RADIUS = new DoubleSpellAttribute("radius", 0.001, 3)
            .addSuggestions(2.0, 5.0, 10.0, 20.0)
            .setFormatter(value -> value + " blocks");

    default void setupRadiused() {
        addAttribute(RADIUS);
    }
}
