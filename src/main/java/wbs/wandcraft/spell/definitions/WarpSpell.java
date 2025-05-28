package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import wbs.utils.util.particles.NormalParticleEffect;
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
        addAttribute(IMPRECISION, 10d);
        addAttribute(RANGE, 50.0);
        addAttribute(SPEED, 2d);
    }

    @Override
    public void configure(DynamicProjectileObject projectile, CastContext context) {
        SpellInstance instance = context.instance();

        RingParticleEffect effect = new RingParticleEffect();

        effect.setRadius(0.5);
        effect.setAmount(3);
        CycleGenerator cycleGenerator = new CycleGenerator(0, 360, instance.getAttribute(SPEED) * 30, 0);
        effect.setRotation(new NumProvider(cycleGenerator));
        effect.setAbout(new VectorProvider(VectorGenerator.buildAnonymous(projectile::getVelocity)));

        Particle particle = instance.getAttribute(PARTICLE, getDefaultParticle());

        projectile.setParticle(new WbsParticleGroup().addEffect(effect, particle));
        projectile.setEndEffects(new WbsParticleGroup().addEffect(new NormalParticleEffect().setXYZ(0).setSpeed(0.18).setAmount(50), particle));

        Player player = context.player();

        SpellTriggeredEvents.ON_HIT_TRIGGER.registerAnonymous(instance, (result) -> {
            player.teleport(result.getHitPosition().toLocation(projectile.world).setDirection(projectile.getVelocity()));
        });
    }

    @Override
    public Particle getDefaultParticle() {
        return Particle.END_ROD;
    }

    @Override
    public Component description() {
        return Component.text(
                "Teleport to a point you're looking at within range."
        );
    }
}
