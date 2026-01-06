package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

/**
 * The specific duration of fire ticks an entity will be lit for
 */
public interface BurnTimeSpell extends ISpellDefinition {
    SpellAttribute<Integer> BURN_TIME = new IntegerSpellAttribute("burn_time", 20)
            .setTicksToSecondsFormatter()
            .overrideTextureValue("duration");

    default void setupBurnTime() {
        addAttribute(BURN_TIME);
    }
}