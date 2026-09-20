package wbs.wandcraft.spell.attributes.attributable;

import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import wbs.wandcraft.context.CastContext;

public interface BurnDamageAttributable extends BurnTimeAttributable, DamageAttributable {
    default double damageAndBurn(Entity entity, CastContext context) {
        return damageAndBurn(entity, context, context.instance().getAttribute(DAMAGE));
    }
    default double damageAndBurn(Entity entity, CastContext context, double damage) {
        int burnTime = context.instance().getAttribute(BURN_TIME);
        return damageAndBurn(entity, context, damage, burnTime);
    }
    default double damageAndBurn(Entity entity, CastContext context, double damage, int burnTime) {
        return damageThen(entity, context, damage, damageable -> {
            igniteUnconditional(damageable, burnTime);
        });
    }

    @AttributableSetupHandler
    default void setupBurnDamage() {
        setAttribute(DAMAGE_TYPE, DamageType.IN_FIRE);
    }
}
