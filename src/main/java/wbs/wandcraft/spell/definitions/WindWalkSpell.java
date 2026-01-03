package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.utils.util.particles.RingParticleEffect;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.extensions.*;
import wbs.wandcraft.spell.definitions.type.SpellType;

public class WindWalkSpell extends SpellDefinition implements CastableSpell, SpeedSpell, DurationalSpell, ParticleSpell, RadiusedSpell {
    private final RingParticleEffect effect = (RingParticleEffect) new RingParticleEffect()
            .setRadius(2);

    public WindWalkSpell() {
        super("wind_walk");

        addSpellType(SpellType.VOID);
        addSpellType(SpellType.ENDER);

        setAttribute(COST, 50);
        setAttribute(COOLDOWN, 10 * Ticks.TICKS_PER_SECOND);

        setAttribute(SPEED, 0.5d);
        setAttribute(DURATION, 2 * Ticks.TICKS_PER_SECOND);
        setAttribute(RADIUS, 3d);
    }

    @Override
    public String rawDescription() {
        return "The caster is pulled into a vortex, moving in the direction they're looking for a short time.";
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
                effectClone.setRotation((i + 10) * 2);
                effectClone.play(getParticle(instance), WbsEntityUtil.getMiddleLocation(caster));

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
        return Particle.GUST;
    }
}
