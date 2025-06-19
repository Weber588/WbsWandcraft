package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import org.bukkit.Particle;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;
import wbs.wandcraft.objects.generics.DynamicProjectileObject;
import wbs.wandcraft.spell.definitions.extensions.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CustomProjectileSpell;
import wbs.wandcraft.spell.definitions.extensions.DamageSpell;

public class ArcaneSparkSpell extends SpellDefinition implements CustomProjectileSpell, DamageSpell {
    private static final WbsParticleEffect EFFECT = new NormalParticleEffect()
            .setXYZ(0.1)
            .setAmount(2);
    private static final WbsParticleEffect END_EFFECT = new NormalParticleEffect()
            .setXYZ(0.1)
            .setSpeed(0.3)
            .setAmount(30);

    public ArcaneSparkSpell() {
        super("arcane_spark");

        setAttribute(DAMAGE, 1d);
        setAttribute(BOUNCES, 4);
        setAttribute(SPEED, 2d);
        setAttribute(IMPRECISION, 30d);
        setAttribute(RANGE, 60d);
        setAttribute(DRAG, 0.03d);
        setAttribute(GRAVITY, 0.1d);
    }

    @Override
    public Component description() {
        return Component.text("Shoots several sparks of arcane energy that bounce and do damage on hit.");
    }

    @Override
    public void cast(CastContext context) {
        shoot(context);
        shoot(context);
        shoot(context);
    }

    @Override
    public void configure(DynamicProjectileObject projectile, CastContext context) {
        SpellInstance instance = context.instance();
        projectile.setParticle(new WbsParticleGroup().addEffect(EFFECT, instance.getAttribute(PARTICLE, getDefaultParticle())));
        projectile.setEndEffects(new WbsParticleGroup().addEffect(END_EFFECT, Particle.FIREWORK));
    }

    @Override
    public Particle getDefaultParticle() {
        return Particle.WAX_OFF;
    }
}
