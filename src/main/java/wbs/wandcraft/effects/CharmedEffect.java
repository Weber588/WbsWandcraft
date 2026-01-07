package wbs.wandcraft.effects;

import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsEventUtils;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.wandcraft.WbsWandcraft;

import java.util.List;

@NullMarked
public class CharmedEffect extends StatusEffect {
    private static final NormalParticleEffect EFFECT = new NormalParticleEffect();
    private static final double FOLLOW_RANGE = 8;

    @Override
    public BossBar.Color barColour() {
        return BossBar.Color.PINK;
    }

    @Override
    public void onApply(LivingEntity entity, StatusEffectInstance instance) {
        if (entity instanceof Mob mob) {
            mob.setTarget(null);
        }
    }

    @Override
    public boolean isValid(LivingEntity entity, StatusEffectInstance instance) {
        return entity.isValid() && entity instanceof Mob;
    }

    @Override
    public boolean onTick(LivingEntity entity, StatusEffectInstance instance) {
        if (Bukkit.getCurrentTick() % 5 == 0) {
            EFFECT.setXYZ(entity.getWidth())
                    .setY(entity.getHeight())
                    .play(Particle.HEART, WbsEntityUtil.getMiddleLocation(entity));
        }

        if (entity instanceof Mob mob) {
            Entity cause = instance.getCauseEntity();
            if (cause != null && cause.isValid() && cause.getWorld().equals(entity.getWorld()) && cause.getLocation().distance(entity.getLocation()) > FOLLOW_RANGE) {
                mob.getPathfinder().moveTo(cause.getLocation());
            }
        }

        return false;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return WbsWandcraft.getKey("charmed");
    }

    @Override
    public void registerEvents() {
        WbsEventUtils.register(WbsWandcraft.getInstance(), EntityTargetLivingEntityEvent.class, this::onTarget);
        WbsEventUtils.register(WbsWandcraft.getInstance(), EntityDamageByEntityEvent.class, this::onAttack);
    }

    private void onTarget(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() == null) {
            return;
        }

        ifPresent(event.getEntity(), instance -> {
            if (event.getTarget().getUniqueId().equals(instance.getCause())) {
                event.setCancelled(true);
            }
        });
    }

    private void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity victim)) {
            return;
        }
        if (!(event.getDamageSource().getCausingEntity() instanceof LivingEntity attacker)) {
            return;
        }

        List<Entity> nearbyEntities = victim.getNearbyEntities(60, 60, 60);

        for (Entity nearby : nearbyEntities) {
            if (nearby.equals(victim) || nearby.equals(attacker)) {
                continue;
            }

            if (nearby instanceof Mob mob) {
                ifPresent(mob, instance -> {
                    if (victim.getUniqueId().equals(instance.getCause())) {
                        mob.setTarget(attacker);
                    } else if (attacker.getUniqueId().equals(instance.getCause())) {
                        mob.setTarget(victim);
                    }
                });
            }
        }
    }
}
