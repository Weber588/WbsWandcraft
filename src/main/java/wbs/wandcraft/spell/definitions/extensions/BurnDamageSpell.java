package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import wbs.wandcraft.context.CastContext;

public interface BurnDamageSpell extends BurnTimeSpell, DamageSpell {
    default void damageAndBurn(Entity entity, CastContext context) {
        damageThen(entity, context, damageable -> {
            int burnTime = context.instance().getAttribute(BURN_TIME);
            damageable.setFireTicks(burnTime);
        });
    }

    @Override
    default DamageType getDamageType() {
        return DamageType.IN_FIRE;
    }
}
