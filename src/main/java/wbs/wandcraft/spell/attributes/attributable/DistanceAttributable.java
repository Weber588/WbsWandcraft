package wbs.wandcraft.spell.attributes.attributable;

import wbs.wandcraft.spell.attributes.AttributeHolder;
import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;

/**
 * Represents a specific distance, as opposed to an upper limit (like Range)
 */
public interface DistanceAttributable extends AttributeHolder {
    SpellAttribute<Double> DISTANCE = new DoubleSpellAttribute("distance", 5)
            .addSuggestions(10.0, 20.0, 50.0, 100.0)
            .overrideTextureValue("range")
            .setNumericFormatter(value -> value + " blocks");

    @AttributableSetupHandler
    default void setupDistance() {
        addAttribute(DISTANCE);
    }
}
