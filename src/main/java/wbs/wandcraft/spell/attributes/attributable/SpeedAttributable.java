package wbs.wandcraft.spell.attributes.attributable;

import net.kyori.adventure.util.Ticks;
import wbs.wandcraft.spell.MagicDomain;
import wbs.wandcraft.spell.attributes.AttributeHolder;
import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;

public interface SpeedAttributable extends AttributeHolder {
    SpellAttribute<Double> SPEED = new DoubleSpellAttribute("speed", 1)
            .setShowAttribute(value -> value != 0)
            .setNumericFormatter(Ticks.TICKS_PER_SECOND, speed -> speed + " blocks/second")
            .domainModifier(MagicDomain.NETHER, AttributeModifierType.MULTIPLY, 1.5)
            .domainModifier(MagicDomain.ENDER, AttributeModifierType.MULTIPLY, 2d);

    @AttributableSetupHandler
    default void setupSpeed() {
        addAttribute(SPEED);
    }
}
