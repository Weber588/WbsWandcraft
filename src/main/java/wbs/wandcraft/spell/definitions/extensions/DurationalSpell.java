package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.AbstractSpellDefinition;

public interface DurationalSpell extends AbstractSpellDefinition {
    SpellAttribute<Integer> DURATION = new IntegerSpellAttribute("duration", 1,20)
            .setFormatter(duration -> duration / 20.0 + " seconds");

    default void setUpDurational() {
        addAttribute(DURATION);
    }
}
