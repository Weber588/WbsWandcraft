package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import wbs.wandcraft.context.CastContext;

public interface BurnDamageSpell extends BurnTimeSpell, DamageSpell {
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

    @Override
    default DamageType getDamageType() {
        return DamageType.IN_FIRE;
    }
}
