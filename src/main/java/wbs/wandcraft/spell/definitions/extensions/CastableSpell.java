package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.AbstractSpellDefinition;

public interface CastableSpell extends AbstractSpellDefinition {
    SpellAttribute<Integer> DELAY = new IntegerSpellAttribute("cast_delay", 0, 5);
    SpellAttribute<Integer> COOLDOWN = new IntegerSpellAttribute("cooldown", 0, 0);

    void cast(CastContext context);

    default void setupCastable() {
        addAttribute(DELAY);
        addAttribute(COOLDOWN);
    }
}
