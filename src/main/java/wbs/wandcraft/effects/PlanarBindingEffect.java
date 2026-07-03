package wbs.wandcraft.effects;

import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsEventUtils;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.RingParticleEffect;
import wbs.wandcraft.WbsWandcraft;

@NullMarked
public class PlanarBindingEffect extends StatusEffect {
    private static final RingParticleEffect EFFECT = (RingParticleEffect) new RingParticleEffect()
            .setAmount(3);
    public static final int ROTATIONS_PER_SECOND = 3;

    @Override
    public boolean onTick(LivingEntity entity, StatusEffectInstance instance) {
        EFFECT.setRadius(entity.getWidth())
                .setRotation((double) Bukkit.getCurrentTick() / Ticks.TICKS_PER_SECOND * 360 / ROTATIONS_PER_SECOND)
                .play(Particle.REVERSE_PORTAL, WbsEntityUtil.getMiddleLocation(entity))
                .play(Particle.SMOKE, WbsEntityUtil.getMiddleLocation(entity));
        return false;
    }

    @Override
    public NamespacedKey getKey() {
        return WbsWandcraft.getKey("planar_binding");
    }

    @Override
    public void registerEvents() {
        WbsEventUtils.register(WbsWandcraft.getInstance(), EntityTeleportEvent.class, this::onTeleport);
        WbsEventUtils.register(WbsWandcraft.getInstance(), PlayerTeleportEvent.class, this::onTeleport);
    }

    private void onTeleport(EntityTeleportEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }
        if (StatusEffectManager.getInstance(entity, this) != null) {
            event.setCancelled(true);
        }
    }
    private void onTeleport(PlayerTeleportEvent event) {
        if (StatusEffectManager.getInstance(event.getPlayer(), this) != null) {
            event.setCancelled(true);
        }
    }
}
