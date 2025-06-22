package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.Particle;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.objects.generics.DynamicProjectileObject;
import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.event.SpellTriggeredEvents;

public interface CustomProjectileSpell extends IProjectileSpell, RangedSpell, ParticleSpell {
    SpellAttribute<Integer> BOUNCES = new IntegerSpellAttribute("bounces", 0)
            .setShowAttribute(value -> value > 0);
    SpellAttribute<Double> GRAVITY = new DoubleSpellAttribute("gravity", 0.16)
            .setShowAttribute(value -> value != 0)
            .setNumericFormatter(20d, value -> value + " blocks/second²");
    SpellAttribute<Double> SIZE = new DoubleSpellAttribute("size",0.3)
            .setNumericFormatter(value -> value + " blocks");
    SpellAttribute<Double> DRAG = new DoubleSpellAttribute("drag",0)
            .setShowAttribute(value -> value != 0)
            .setNumericFormatter(20d, value -> value + " blocks/second²");

    default void setupCustomProjectile() {
        addAttribute(BOUNCES);
        addAttribute(GRAVITY);
        addAttribute(SIZE);
        addAttribute(DRAG);
    }

    @Override
    default void cast(CastContext context) {
        shoot(context);
    }

    default void shoot(CastContext context) {
        Player player = context.player();
        SpellInstance instance = context.instance();

        DynamicProjectileObject projectile = buildProjectile(context, instance, player);

        projectile.spawn();
    }

    private @NotNull DynamicProjectileObject buildProjectile(CastContext context, SpellInstance instance, Player player) {
        Double range = instance.getAttribute(RANGE);
        Double speed = instance.getAttribute(SPEED);
        Double drag = instance.getAttribute(DRAG);
        Double gravity = instance.getAttribute(GRAVITY);
        Particle particle = instance.getAttribute(PARTICLE, getDefaultParticle());
        double hitboxSize = instance.getAttribute(SIZE);
        int bounces = instance.getAttribute(BOUNCES);

        DynamicProjectileObject projectile = new DynamicProjectileObject(context.location(), player, context);
        WbsParticleGroup tickEffects = new WbsParticleGroup();

        projectile.setHitBoxSize(hitboxSize);
        tickEffects.addEffect(new NormalParticleEffect().setXYZ(hitboxSize).setAmount(3), particle);

        projectile.setRange(range);
        projectile.setVelocity(getDirection(context, speed));
        projectile.setDrag(drag);
        projectile.setParticle(tickEffects);
        projectile.setGravity(gravity);

        if (bounces > 0) {
            projectile.setDoBounces(true);
            projectile.setMaxBounces(bounces);
        }

        configureWithDefaultHitTrigger(projectile, context);

        return projectile;
    }

    private void configureWithDefaultHitTrigger(DynamicProjectileObject projectile, CastContext context) {
        configure(projectile, context);

        projectile.setOnHit(result -> {
            context.runEffects(SpellTriggeredEvents.ON_HIT_TRIGGER, result);

            if (this instanceof DamageSpell && result.getHitEntity() instanceof Damageable hitEntity) {
                double damage = context.instance().getAttribute(DamageSpell.DAMAGE);

                DamageSource source = DamageSource.builder(DamageType.INDIRECT_MAGIC)
                        .withDirectEntity(context.player())
                        .withDamageLocation(projectile.location)
                        .build();

                hitEntity.damage(damage, source);
            }
            return true;
        });
    }

    void configure(DynamicProjectileObject projectile, CastContext context);
}
