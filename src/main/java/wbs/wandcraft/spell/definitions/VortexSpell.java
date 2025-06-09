package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.utils.util.particles.RingParticleEffect;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.definitions.extensions.*;

public class VortexSpell extends SpellDefinition implements CastableSpell, SpeedSpell, DurationalSpell, ParticleSpell, RadiusedSpell {
    private final RingParticleEffect effect = (RingParticleEffect) new RingParticleEffect()
            .setRadius(1)
            .setAmount(3);

    public VortexSpell() {
        super("vortex");

        addAttribute(SPEED, 0.5d);
        addAttribute(DURATION, 40);
        addAttribute(RADIUS, 3d);
    }

    @Override
    public Component description() {
        return Component.text("The caster is pulled into a vortex, moving in the direction they're looking for a short time.");
    }

    @Override
    public void cast(CastContext context) {
        Player caster = context.player();
        SpellInstance instance = context.instance();

        RingParticleEffect effectClone = effect.clone();
        RadiusSelector<LivingEntity> selector = new RadiusSelector<>(LivingEntity.class)
                .setRange(instance.getAttribute(RADIUS));

        new BukkitRunnable() {
            int i = 0;

            public void run() {
                i++;
                if (i > instance.getAttribute(DURATION)) {
                    cancel();
                }

                Double speed = instance.getAttribute(SPEED);
                Vector direction = WbsEntityUtil.getFacingVector(caster, speed);
                effectClone.setAbout(direction);
                effectClone.setRotation(i*2);
                effectClone.play(instance.getAttribute(PARTICLE, getDefaultParticle()), WbsEntityUtil.getMiddleLocation(caster));

                caster.setVelocity(direction);

                selector.selectExcluding(caster).forEach(entity -> {
                    Vector pullVector = caster.getLocation().subtract(entity.getLocation()).toVector();
                    pullVector.normalize().multiply(speed);

                    entity.setVelocity(entity.getVelocity().add(pullVector));
                });
            }
        }.runTaskTimer(WbsWandcraft.getInstance(), 0L, 1L);
    }

    @Override
    public Particle getDefaultParticle() {
        return Particle.SMALL_GUST;
    }
}
