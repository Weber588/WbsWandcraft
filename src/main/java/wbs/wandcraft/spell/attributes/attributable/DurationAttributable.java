package wbs.wandcraft.spell.attributes.attributable;

import net.kyori.adventure.util.Ticks;
import wbs.wandcraft.AttributeDataType;
import wbs.wandcraft.spell.MagicDomain;
import wbs.wandcraft.spell.attributes.AttributeHolder;
import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;

public interface DurationAttributable extends AttributeHolder {
    double ARCANE_MULTIPLIER = 1.25;
    double NATURE_MULTIPLIER = 1.1;
    double NETHER_MULTIPLIER = 0.75;
    SpellAttribute<Integer> DURATION = new IntegerSpellAttribute("duration", Ticks.TICKS_PER_SECOND)
            .setTicksToSecondsFormatter()
            .domainModifier(MagicDomain.ARCANE, AttributeModifierType.MULTIPLY, AttributeDataType.DOUBLE, ARCANE_MULTIPLIER)
            .domainModifier(MagicDomain.NATURE, AttributeModifierType.MULTIPLY, AttributeDataType.DOUBLE, NATURE_MULTIPLIER)
            .domainModifier(MagicDomain.NETHER, AttributeModifierType.MULTIPLY, AttributeDataType.DOUBLE, NETHER_MULTIPLIER);

    @AttributableSetupHandler
    default void setUpDurational() {
        addAttribute(DURATION);
    }
}
