package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.damage.DamageType;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.SpellInstance;

public interface BurnDamageSpell extends BurnTimeSpell, DamageSpell {
    default void damageAndBurn(Entity entity, CastContext context) {
        if (entity instanceof Damageable damageable) {
            SpellInstance instance = context.instance();
            int burnTime = instance.getAttribute(BURN_TIME);

            double health = damageable.getHealth();
            damage(context, damageable, DamageType.IN_FIRE);
            if (health != damageable.getHealth()) {
                damageable.setFireTicks(burnTime);
            }
        }
    }
}
