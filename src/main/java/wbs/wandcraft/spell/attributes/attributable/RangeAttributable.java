package wbs.wandcraft.spell.attributes.attributable;

import wbs.wandcraft.spell.attributes.AttributeHolder;
import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;

public interface RangeAttributable extends AttributeHolder {
    SpellAttribute<Double> RANGE = new DoubleSpellAttribute("range", 40)
            .addSuggestions(10.0, 20.0, 50.0, 100.0)
            .setNumericFormatter(value -> value + " blocks")
            .setShowAttribute(value -> value > 0);

    @AttributableSetupHandler
    default void setupRanged() {
        addAttribute(RANGE);
    }
}
