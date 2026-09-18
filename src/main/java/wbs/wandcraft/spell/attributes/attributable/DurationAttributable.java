package wbs.wandcraft.spell.attributes.attributable;

import net.kyori.adventure.util.Ticks;
import wbs.wandcraft.spell.attributes.AttributeHolder;
import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;

public interface DurationAttributable extends AttributeHolder {
    SpellAttribute<Integer> DURATION = new IntegerSpellAttribute("duration", Ticks.TICKS_PER_SECOND)
            .setTicksToSecondsFormatter();

    @AttributableSetupHandler
    default void setUpDurational() {
        addAttribute(DURATION);
    }
}
