package wbs.wandcraft.spell.attributes.attributable;

import wbs.wandcraft.spell.attributes.AttributeHolder;
import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;

public interface RadiusAttributable extends AttributeHolder {
    SpellAttribute<Double> RADIUS = new DoubleSpellAttribute("radius", 3)
            .addSuggestions(2.0, 5.0, 10.0, 20.0)
            .overrideTextureValue("range")
            .setNumericFormatter(value -> value + " blocks");

    @AttributableSetupHandler
    default void setupRadiused() {
        addAttribute(RADIUS);
    }
}
