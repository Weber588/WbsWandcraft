package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.event.SpellTriggeredEvent;
import wbs.wandcraft.objects.generics.DynamicProjectileObject;

public interface CustomProjectileSpell extends AbstractProjectileSpell, RangedSpell, CastableSpell {
    SpellTriggeredEvent<RayTraceResult> ON_HIT_TRIGGER = new SpellTriggeredEvent<>(WbsWandcraft.getKey("on_hit"), RayTraceResult.class);

    @Override
    default void cast(CastContext context) {
        Player player = context.player();
        SpellInstance instance = context.instance();

        Double range = instance.getAttribute(RANGE);
        Double speed = instance.getAttribute(SPEED);

        DynamicProjectileObject projectile = new DynamicProjectileObject(player.getEyeLocation(), player, instance);
        projectile.setRange(range);
        projectile.setVelocity(WbsEntityUtil.getFacingVector(player, speed));

        projectile.setOnHit(result -> {
            instance.getEffects(ON_HIT_TRIGGER).forEach(effect -> effect.run(instance, result));
            return true;
        });

        configure(projectile, context);

        projectile.spawn();
    }

    void configure(DynamicProjectileObject projectile, CastContext context);
}
