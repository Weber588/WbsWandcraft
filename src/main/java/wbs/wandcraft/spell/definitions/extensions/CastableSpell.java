package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.AbstractSpellDefinition;

public interface CastableSpell extends AbstractSpellDefinition {
    SpellAttribute<Integer> DELAY = new IntegerSpellAttribute("cast_delay", 0, 4)
            .setFormatter(delay -> delay / 20.0 + " seconds");
    SpellAttribute<Integer> COOLDOWN = new IntegerSpellAttribute("cooldown", 0, 5)
            .setFormatter(cooldown -> cooldown / 20.0 + " seconds");

    void cast(CastContext context);

    default void setupCastable() {
        addAttribute(DELAY);
        addAttribute(COOLDOWN);
    }
}
