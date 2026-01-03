package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
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

import static wbs.wandcraft.spell.definitions.type.SpellType.ARCANE;
import static wbs.wandcraft.spell.definitions.type.SpellType.VOID;

public class ArcaneBurstSpell extends SpellDefinition implements CustomProjectileSpell, DamageSpell, RadiusedSpell, ForceSpell {
    private static final WbsParticleGroup EXPLODE_GROUP = new WbsParticleGroup();

    static {
        NormalParticleEffect explodeEffect = new NormalParticleEffect();
        explodeEffect.setXYZ(0);
        explodeEffect.setAmount(250);
        explodeEffect.setSpeed(1.5);
        NormalParticleEffect explodeEffect2 = new NormalParticleEffect();
        explodeEffect2.setXYZ(0);
        explodeEffect2.setAmount(500);
        explodeEffect2.setSpeed(0.3);

        EXPLODE_GROUP.addEffect(explodeEffect, Particle.CRIT);
        EXPLODE_GROUP.addEffect(explodeEffect2, Particle.SMOKE);
    }

    public ArcaneBurstSpell() {
        super("arcane_burst");

        addSpellType(ARCANE);
        addSpellType(VOID);

        setAttribute(COST, 350);
        setAttribute(COOLDOWN, 10 * Ticks.TICKS_PER_SECOND);

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
        // effect.setData(new Particle.DustTransition(ARCANE.color(), ARCANE.mulColor(2), 0.75f));

        Particle particle = getParticle(instance);

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
        return Particle.CRIT;
    }

    @Override
    public String rawDescription() {
        return "A burst of arcane energy that spirals and ";
    }
}
