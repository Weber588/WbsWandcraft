package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.SpellInstance;

public interface BurnDamageSpell extends BurnTimeSpell, DamageSpell {
    default void damageAndBurn(Entity entity, CastContext context) {
        if (entity instanceof Damageable damageable) {
            SpellInstance instance = context.instance();
            Player player = context.player();
            double damage = instance.getAttribute(DAMAGE);
            int burnTime = instance.getAttribute(BURN_TIME);

            DamageSource source = DamageSource.builder(DamageType.IN_FIRE)
                    .withDirectEntity(player)
                    .build();

            double health = damageable.getHealth();
            damageable.damage(damage, source);
            if (health != damageable.getHealth()) {
                damageable.setFireTicks(burnTime);
            }
        }
    }
}
