package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

public interface DurationalSpell extends ISpellDefinition {
    SpellAttribute<Integer> DURATION = new IntegerSpellAttribute("duration", 20)
            .setTicksToSecondsFormatter();

    default void setUpDurational() {
        addAttribute(DURATION);
    }
}
