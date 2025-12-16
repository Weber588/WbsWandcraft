package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

public interface CastableSpell extends ISpellDefinition {
    SpellAttribute<Integer> DELAY = new IntegerSpellAttribute("cast_delay", 4)
            .setShowAttribute(delay -> delay > 0)
            .setTicksToSecondsFormatter();
    SpellAttribute<Integer> COOLDOWN = new IntegerSpellAttribute("cooldown", 5)
            .setShowAttribute(cooldown -> cooldown > 0)
            .setTicksToSecondsFormatter();
    SpellAttribute<Integer> COST = new IntegerSpellAttribute("cost", 100)
            .setShowAttribute(cost -> cost > 0);

    void cast(CastContext context);

    default void setupCastable() {
        addAttribute(DELAY);
        addAttribute(COOLDOWN);
        addAttribute(COST);
    }

    // Implementing classes may override this to return false, to indicate that the spell does not immediately complete.
    // Classes that do this must invoke CastContext#finish upon completion to indicate the spell has finished casting.
    default boolean completeAfterCast() {
        return true;
    }
}
