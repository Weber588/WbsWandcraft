package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

public interface RadiusedSpell extends ISpellDefinition {
    SpellAttribute<Double> RADIUS = new DoubleSpellAttribute("radius", 3)
            .addSuggestions(2.0, 5.0, 10.0, 20.0)
            .overrideTextureValue("range")
            .setNumericFormatter(value -> value + " blocks");

    default void setupRadiused() {
        addAttribute(RADIUS);
    }
}
