package wbs.wandcraft.spell.attributes.attributable;

import net.kyori.adventure.util.Ticks;
import wbs.wandcraft.spell.attributes.AttributeHolder;
import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;

public interface ForceAttributable extends AttributeHolder {
    SpellAttribute<Double> FORCE = new DoubleSpellAttribute("force", 1)
            .setShowAttribute(value -> value != 0)
            .overrideTextureValue("speed")
            .setNumericFormatter(Ticks.TICKS_PER_SECOND, speed -> speed + " blocks/second");

    @AttributableSetupHandler
    default void setupForce() {
        addAttribute(FORCE);
    }
}
