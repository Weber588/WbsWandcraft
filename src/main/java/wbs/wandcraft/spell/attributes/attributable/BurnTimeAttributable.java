package wbs.wandcraft.spell.attributes.attributable;

import net.kyori.adventure.util.Ticks;
import wbs.wandcraft.spell.attributes.AttributeHolder;
import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;

/**
 * The specific duration of fire ticks an entity will be lit for
 */
public interface BurnTimeAttributable extends AttributeHolder {
    SpellAttribute<Integer> BURN_TIME = new IntegerSpellAttribute("burn_time", Ticks.TICKS_PER_SECOND)
            .setTicksToSecondsFormatter()
            .overrideTextureValue("duration");

    @AttributableSetupHandler
    default void setupBurnTime() {
        addAttribute(BURN_TIME);
    }
}