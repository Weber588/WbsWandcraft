package wbs.wandcraft.spell.attributes.attributable;

import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import wbs.wandcraft.context.CastContext;

public interface BurnDamageAttributable extends BurnTimeAttributable, DamageAttributable {
    default void damageAndBurn(Entity entity, CastContext context) {
        damageAndBurn(entity, context, context.instance().getAttribute(DAMAGE));
    }
    default void damageAndBurn(Entity entity, CastContext context, double damage) {
        int burnTime = context.instance().getAttribute(BURN_TIME);
        damageAndBurn(entity, context, damage, burnTime);
    }
    default void damageAndBurn(Entity entity, CastContext context, double damage, int burnTime) {
        damageThen(entity, context, damage, damageable -> {
            damageable.setFireTicks(burnTime);
        });
    }

    @AttributableSetupHandler
    default void setupBurnDamage() {
        setAttribute(DAMAGE_TYPE, DamageType.IN_FIRE);
    }
}
