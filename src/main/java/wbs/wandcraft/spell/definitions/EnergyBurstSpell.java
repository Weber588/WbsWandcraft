package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import org.bukkit.Particle;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.RingParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;
import wbs.utils.util.pluginhooks.WbsRegionUtils;
import wbs.utils.util.providers.NumProvider;
import wbs.utils.util.providers.VectorProvider;
import wbs.utils.util.providers.generator.num.CycleGenerator;
import wbs.utils.util.providers.generator.vector.VectorGenerator;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.objects.generics.DynamicProjectileObject;
import wbs.wandcraft.spell.definitions.extensions.CustomProjectileSpell;
import wbs.wandcraft.spell.definitions.extensions.DamageSpell;
import wbs.wandcraft.spell.definitions.extensions.ForceSpell;
import wbs.wandcraft.spell.definitions.extensions.RadiusedSpell;
import wbs.wandcraft.spell.event.SpellTriggeredEvents;

import java.util.Collection;

public class EnergyBurstSpell extends SpellDefinition implements CustomProjectileSpell, DamageSpell, RadiusedSpell, ForceSpell {
    private static final WbsParticleGroup EXPLODE_GROUP = new WbsParticleGroup();

    static {
        NormalParticleEffect explodeEffect = new NormalParticleEffect();
        explodeEffect.setXYZ(0);
        explodeEffect.setAmount(500);
        explodeEffect.setSpeed(1);

        Particle explodeParticle = Particle.TOTEM_OF_UNDYING;
        EXPLODE_GROUP.addEffect(explodeEffect, explodeParticle);
    }

    public EnergyBurstSpell() {
        super("energy_burst");

        setAttribute(COST, 150);
        setAttribute(SPEED, 5d);
        setAttribute(FORCE, 1.5);
        setAttribute(DAMAGE, 6.0);
        setAttribute(RANGE, 100.0);
        setAttribute(IMPRECISION, 2d);
        setAttribute(RADIUS, 3d);
    }

    @Override
    public void configure(DynamicProjectileObject projectile, CastContext context) {
        SpellInstance instance = context.instance();

        RingParticleEffect effect = new RingParticleEffect();

        effect.setRadius(0.25);
        effect.setAmount(2);
        CycleGenerator cycleGenerator = new CycleGenerator(0, 360, instance.getAttribute(SPEED) * 30, 0);
        effect.setRotation(new NumProvider(cycleGenerator));
        effect.setAbout(new VectorProvider(VectorGenerator.buildAnonymous(projectile::getVelocity)));

        Particle particle = instance.getAttribute(PARTICLE, getDefaultParticle());

        projectile.setParticle(new WbsParticleGroup().addEffect(effect, particle));
        projectile.setEndEffects(EXPLODE_GROUP);

        SpellTriggeredEvents.OBJECT_EXPIRE_TRIGGER.registerAnonymous(instance, (result) -> {
            Collection<LivingEntity> hit = new RadiusSelector<>(LivingEntity.class)
                    .setRange(instance.getAttribute(RADIUS))
                    .exclude(context.player())
                    .select(result);

            DamageSource source = DamageSource.builder(DamageType.INDIRECT_MAGIC)
                    .withDirectEntity(context.player())
                    .build();

            Vector pushVector = new Vector(0, instance.getAttribute(FORCE), 0);
            for (LivingEntity target : hit) {
                if (WbsRegionUtils.canDealDamage(context.player(), target)) {
                    target.setVelocity(pushVector);
                    target.damage(instance.getAttribute(DAMAGE), source);
                    target.setVelocity(pushVector);
                }
            }
        });
    }

    @Override
    public Particle getDefaultParticle() {
        return Particle.HAPPY_VILLAGER;
    }

    @Override
    public Component description() {
        return Component.text(
                "The most simple projectile spell that fires a blast of energy in the direction the casterUUID is facing, dealing damage to anything hit."
        );
    }
}
