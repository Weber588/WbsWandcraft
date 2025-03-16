package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import wbs.utils.util.WbsMath;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;
import wbs.wandcraft.objects.generics.DynamicProjectileObject;
import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.event.SpellTriggeredEvents;

public interface CustomProjectileSpell extends AbstractProjectileSpell, RangedSpell, ParticleSpell {
    SpellAttribute<Integer> BOUNCES = new IntegerSpellAttribute("bounces", 0, 0)
            .setShowAttribute(value -> value > 0);
    SpellAttribute<Double> GRAVITY = new DoubleSpellAttribute("gravity", 0, 3)
            .setShowAttribute(value -> value > 0)
            .setFormatter(value -> value + " blocks/second²");
    SpellAttribute<Double> SIZE = new DoubleSpellAttribute("size", 0.01, 0.3);

    default void setupCustomProjectile() {
        addAttribute(BOUNCES);
        addAttribute(GRAVITY);
        addAttribute(SIZE);
    }

    @Override
    default void cast(CastContext context) {
        Player player = context.player();
        SpellInstance instance = context.instance();

        Double range = instance.getAttribute(RANGE);
        Double speed = instance.getAttribute(SPEED);
        Double gravity = instance.getAttribute(GRAVITY);
        Particle particle = instance.getAttribute(PARTICLE, getDefaultParticle());
        double hitboxSize = instance.getAttribute(SIZE);
        int bounces = instance.getAttribute(BOUNCES);

        DynamicProjectileObject projectile = new DynamicProjectileObject(player.getEyeLocation(), player, instance);
        WbsParticleGroup tickEffects = new WbsParticleGroup();

        projectile.setHitBoxSize(hitboxSize);
        tickEffects.addEffect(new NormalParticleEffect().setXYZ(hitboxSize).setAmount(3), particle);

        projectile.setRange(range);
        projectile.setVelocity(WbsMath.scaleVector(getDirection(context), speed));
        projectile.setParticle(tickEffects);

        projectile.setGravityInSeconds(gravity);
        if (bounces > 0) {
            projectile.setDoBounces(true);
            projectile.setMaxBounces(bounces);
        }

        configure(projectile, context);

        projectile.setOnHit(result -> {
            instance.getEffects(SpellTriggeredEvents.ON_HIT_TRIGGER).forEach(effect -> effect.run(instance, result));
            return true;
        });

        projectile.spawn();
    }

    void configure(DynamicProjectileObject projectile, CastContext context);
}
