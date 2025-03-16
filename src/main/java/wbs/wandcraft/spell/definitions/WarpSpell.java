package wbs.wandcraft.spell.definitions;

import org.bukkit.Particle;
import wbs.utils.util.particles.RingParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;
import wbs.utils.util.providers.NumProvider;
import wbs.utils.util.providers.VectorProvider;
import wbs.utils.util.providers.generator.num.CycleGenerator;
import wbs.utils.util.providers.generator.vector.VectorGenerator;
import wbs.wandcraft.objects.generics.DynamicProjectileObject;
import wbs.wandcraft.spell.definitions.extensions.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CustomProjectileSpell;
import wbs.wandcraft.spell.event.SpellTriggeredEvents;

public class WarpSpell extends SpellDefinition implements CustomProjectileSpell {
    public WarpSpell() {
        super("warp");

        addAttribute(SIZE, 0.0);
        addAttribute(GRAVITY, 0.0);
        addAttribute(ACCURACY, 100.0);
    }

    @Override
    public double getMaxAngle() {
        return 20;
    }

    @Override
    public void configure(DynamicProjectileObject projectile, CastContext context) {
        SpellInstance instance = context.instance();

        RingParticleEffect effect = new RingParticleEffect();

        effect.setRadius(0.5);
        effect.setAmount(3);
        CycleGenerator cycleGenerator = new CycleGenerator(0, 360, 90, 0);
        effect.setRotation(new NumProvider(cycleGenerator));
        effect.setAbout(new VectorProvider(VectorGenerator.buildAnonymous(projectile::getVelocity)));
        projectile.setParticle(new WbsParticleGroup().addEffect(effect, instance.getAttribute(PARTICLE, getDefaultParticle())));

        SpellTriggeredEvents.ON_HIT_TRIGGER.registerAnonymous(instance, (result) -> {
            context.player().teleport(result.getHitPosition().toLocation(projectile.world));
        });
    }

    @Override
    public Particle getDefaultParticle() {
        return Particle.END_ROD;
    }
}
