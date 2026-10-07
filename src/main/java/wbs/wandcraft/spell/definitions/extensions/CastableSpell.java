package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.AttributeDataType;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.MagicDomain;
import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.attributable.AttributableSetupHandler;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

public interface CastableSpell extends ISpellDefinition {
    double SCULK_MULTIPLIER = 0.8;

    // TODO: Populate cooldown for all spells
    SpellAttribute<Integer> COOLDOWN = new IntegerSpellAttribute("cooldown", 5)
            .setShowAttribute(cooldown -> cooldown > 0)
            .setTicksToSecondsFormatter()
            .overrideTextureValue("duration")
            .sentiment(SpellAttribute.Sentiment.NEGATIVE)
            .domainModifier(MagicDomain.ARCANE, AttributeModifierType.MULTIPLY, AttributeDataType.DOUBLE, 0.8);

    SpellAttribute<Integer> COST = new IntegerSpellAttribute("cost", 100)
            .setShowAttribute(cost -> cost > 0)
            .sentiment(SpellAttribute.Sentiment.NEGATIVE)
            .domainModifier(MagicDomain.SCULK, AttributeModifierType.MULTIPLY, AttributeDataType.DOUBLE, SCULK_MULTIPLIER);

    void cast(CastContext context);

    @AttributableSetupHandler
    default void setupCastable() {
        addAttribute(COOLDOWN);
        addAttribute(COST);
    }

    // Implementing classes may override this to return false, to indicate that the spell does not immediately complete.
    // Classes that do this must invoke CastContext#finish upon completion to indicate the spell has finished casting.
    default boolean completeAfterCast() {
        return true;
    }

    default boolean requiresConcentration() {
        return false;
    }
}
