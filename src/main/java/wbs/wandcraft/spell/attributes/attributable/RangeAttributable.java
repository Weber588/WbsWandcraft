package wbs.wandcraft.spell.attributes.attributable;

import wbs.wandcraft.spell.MagicDomain;
import wbs.wandcraft.spell.attributes.AttributeHolder;
import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;

public interface RangeAttributable extends AttributeHolder {
    Double ARCANE_MULTIPLIER = 1.5;
    Double ENDER_MULTIPLIER = 1.5;

    SpellAttribute<Double> RANGE = new DoubleSpellAttribute("range", 40)
            .addSuggestions(10.0, 20.0, 50.0, 100.0)
            .setNumericFormatter(value -> value + " blocks")
            .setShowAttribute(value -> value > 0)
            .domainModifier(MagicDomain.ARCANE, AttributeModifierType.MULTIPLY, ARCANE_MULTIPLIER)
            .domainModifier(MagicDomain.ENDER, AttributeModifierType.MULTIPLY, ENDER_MULTIPLIER);

    @AttributableSetupHandler
    default void setupRanged() {
        addAttribute(RANGE);
    }
}
