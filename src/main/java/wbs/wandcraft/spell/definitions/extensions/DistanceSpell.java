package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

/**
 * Represents a specific distance, as opposed to an upper limit (like Range)
 */
public interface DistanceSpell extends ISpellDefinition {
    SpellAttribute<Double> DISTANCE = new DoubleSpellAttribute("distance", 5)
            .addSuggestions(10.0, 20.0, 50.0, 100.0)
            .overrideTextureValue("range")
            .setNumericFormatter(value -> value + " blocks");

    default void setupDistance() {
        addAttribute(DISTANCE);
    }
}
