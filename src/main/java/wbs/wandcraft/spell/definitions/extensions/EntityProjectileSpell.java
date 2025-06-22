package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.CreatureSpawnEvent;
import wbs.utils.util.WbsMath;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.SpellInstance;

public interface EntityProjectileSpell<T extends Projectile> extends IProjectileSpell {
    Class<T> getProjectileClass();

    @Override
    default void cast(CastContext context) {
        SpellInstance instance = context.instance();

        Double speed = instance.getAttribute(SPEED);

        context.player().getWorld().spawn(
                context.player().getEyeLocation(),
                getProjectileClass(),
                CreatureSpawnEvent.SpawnReason.SPELL,
                projectile -> {
                    projectile.setVelocity(WbsMath.scaleVector(getDirection(context), speed));
                    configure(projectile, context);
                }
        );
    }

    void configure(T t, CastContext context);
}
