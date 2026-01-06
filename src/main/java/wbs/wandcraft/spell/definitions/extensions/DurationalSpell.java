package wbs.wandcraft.spell.definitions.extensions;

import net.kyori.adventure.util.Ticks;
import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

public interface DurationalSpell extends ISpellDefinition {
    SpellAttribute<Integer> DURATION = new IntegerSpellAttribute("duration", Ticks.TICKS_PER_SECOND)
            .setTicksToSecondsFormatter();

    default void setUpDurational() {
        addAttribute(DURATION);
    }
}
