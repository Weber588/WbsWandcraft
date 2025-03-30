package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

public interface CastableSpell extends ISpellDefinition {
    SpellAttribute<Integer> DELAY = new IntegerSpellAttribute("cast_delay", 0, 4)
            .setFormatter(delay -> delay / 20.0 + " seconds");
    SpellAttribute<Integer> COOLDOWN = new IntegerSpellAttribute("cooldown", 0, 5)
            .setFormatter(cooldown -> cooldown / 20.0 + " seconds");

    void cast(CastContext context);

    default void setupCastable() {
        addAttribute(DELAY);
        addAttribute(COOLDOWN);
    }

    // Implementing classes may override this to return false, to indicate that the spell does not immediately complete.
    // Classes that do this must invoke CastContext#finish upon completion to indicate the spell has finished casting.
    default boolean completeAfterCast() {
        return true;
    }
}
