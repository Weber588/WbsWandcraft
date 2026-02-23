package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
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
            .setShowAttribute(value -> value > 0)
            .sentiment(SpellAttribute.Sentiment.NEUTRAL);
    SpellAttribute<Double> GRAVITY = new DoubleSpellAttribute("gravity", 0.16)
            .setShowAttribute(value -> value != 0)
            .setNumericFormatter(20d, value -> value + " blocks/second²")
            .sentiment(SpellAttribute.Sentiment.NEUTRAL);
    SpellAttribute<Double> SIZE = new DoubleSpellAttribute("size",0.3)
            .setNumericFormatter(value -> value + " blocks")
            .sentiment(SpellAttribute.Sentiment.NEUTRAL);
    SpellAttribute<Double> DRAG = new DoubleSpellAttribute("drag",0)
            .setShowAttribute(value -> value != 0)
            .setNumericFormatter(20d, value -> value + " blocks/second²")
            .sentiment(SpellAttribute.Sentiment.NEUTRAL);

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
        Particle particle = getParticle(instance);
        double hitboxSize = instance.getAttribute(SIZE);
        int bounces = instance.getAttribute(BOUNCES);

        DynamicProjectileObject projectile = buildProjectile(context, player);
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

    default @NotNull DynamicProjectileObject buildProjectile(CastContext context, Player player) {
        return new DynamicProjectileObject(context.location(), player, context);
    }

    private void configureWithDefaultHitTrigger(DynamicProjectileObject projectile, CastContext context) {
        configure(projectile, context);

        projectile.setOnHit(result -> {
            context.runEffects(SpellTriggeredEvents.ON_HIT_TRIGGER, result);

            boolean expire = false;

            Entity hitEntity = result.getHitEntity();
            Block hitBlock = result.getHitBlock();
            if (hitEntity != null) {
                if (this instanceof DamageSpell damageSpell && hitEntity instanceof Damageable damageable) {
                    double damage = context.instance().getAttribute(DamageSpell.DAMAGE);

                    DamageSource.Builder damageSource = damageSpell.buildDamageSource(context, DamageType.INDIRECT_MAGIC);
                    damageSource.withDamageLocation(projectile.location);

                    damageable.damage(damage, damageSource.build());
                }

                expire |= expireOnHitEntity();
            }

            if (hitBlock != null) {
                expire |= expireOnHitBlock();
            }

            return expire;
        });
    }

    void configure(DynamicProjectileObject projectile, CastContext context);
    default boolean expireOnHitBlock() {
        return true;
    }

    default boolean expireOnHitEntity() {
        return true;
    }
}
