package wbs.wandcraft.effects;

import io.papermc.paper.registry.keys.tags.EntityTypeTagKeys;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsEventUtils;
import wbs.utils.util.WbsRegistryUtil;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.wandcraft.WbsWandcraft;

@NullMarked
public class DeathWalkEffect extends StatusEffect {
    private static final NormalParticleEffect EFFECT = (NormalParticleEffect) new NormalParticleEffect()
            .setData(Material.GRAVEL.createBlockData());

    @Override
    public boolean onTick(LivingEntity entity, StatusEffectInstance instance) {
        EFFECT.setXYZ(entity.getWidth())
                .setY(entity.getHeight())
                .play(Particle.FALLING_DUST, WbsEntityUtil.getMiddleLocation(entity));
        return false;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return WbsWandcraft.getKey("death_walk");
    }

    @Override
    public void registerEvents() {
        WbsEventUtils.register(WbsWandcraft.getInstance(), EntityTargetLivingEntityEvent.class, this::onUndeadTarget);
    }

    private void onUndeadTarget(EntityTargetLivingEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (!WbsRegistryUtil.isTagged(entity.getType(), EntityTypeTagKeys.UNDEAD)) {
            return;
        }

        LivingEntity target = event.getTarget();
        if (target != null && StatusEffectManager.getInstance(target, this) != null) {
            event.setCancelled(true);
        }
    }
}
