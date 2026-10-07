package wbs.wandcraft.spell.attributes.attributable;

import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.Damageable;
import wbs.utils.util.pluginhooks.WbsRegionUtils;
import wbs.wandcraft.AttributeDataType;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.MagicDomain;
import wbs.wandcraft.spell.attributes.AttributeHolder;
import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;

/**
 * The specific duration of fire ticks an entity will be lit for
 */
public interface BurnTimeAttributable extends AttributeHolder {
    SpellAttribute<Integer> BURN_TIME = new IntegerSpellAttribute("burn_time", Ticks.TICKS_PER_SECOND)
            .setTicksToSecondsFormatter()
            .overrideTextureValue("duration")
            .domainModifiers(MagicDomain.NETHER, Ticks.TICKS_PER_SECOND, null, Ticks.TICKS_PER_SECOND)
            .domainModifier(MagicDomain.VOID, AttributeModifierType.MULTIPLY, AttributeDataType.DOUBLE, 0.5d);

    @AttributableSetupHandler
    default void setupBurnTime() {
        addAttribute(BURN_TIME);
    }

    default void ignite(Damageable damageable, CastContext context) {
        int burnTime = context.instance().getAttribute(BURN_TIME);
        ignite(damageable, context, burnTime);
    }
    default void ignite(Damageable damageable, CastContext context, int burnTime) {
        if (WbsRegionUtils.canDealDamage(context.player(), damageable)) {
            igniteUnconditional(damageable, burnTime);
        }
    }

    default void igniteUnconditional(Damageable damageable, CastContext context) {
        int burnTime = context.instance().getAttribute(BURN_TIME);
        igniteUnconditional(damageable, burnTime);
    }
    default void igniteUnconditional(Damageable damageable, int burnTime) {
        damageable.setFireTicks(burnTime);
    }
}