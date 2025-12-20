package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Particle;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;
import wbs.wandcraft.objects.generics.DynamicProjectileObject;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CustomProjectileSpell;
import wbs.wandcraft.spell.definitions.extensions.DamageSpell;

public class EldritchBlastSpell extends SpellDefinition implements CustomProjectileSpell, DamageSpell {
    private static final WbsParticleGroup EFFECT = new WbsParticleGroup();
    private static final WbsParticleGroup END_EFFECT = new WbsParticleGroup();

    static {
        NormalParticleEffect effect = new NormalParticleEffect();
        double size = 0.1;
        int particleAmount = (int) (size * 25);
        effect.setAmount(particleAmount);
        effect.setXYZ(size);
        Particle.DustOptions data = new Particle.DustOptions(Color.fromRGB(100, 0, 150), 0.8F);
        effect.setOptions(data);

        NormalParticleEffect coreEffect = new NormalParticleEffect();
        coreEffect.setAmount((int) Math.ceil(particleAmount / 3.0));
        coreEffect.setXYZ(0.1);

        NormalParticleEffect endEffect = new NormalParticleEffect();
        endEffect.setAmount(50);
        endEffect.setXYZ(size);
        endEffect.setSpeed(0.1);

        Particle particle = Particle.DUST;
        EFFECT.addEffect(effect, particle);
        Particle core = Particle.SMOKE;
        EFFECT.addEffect(coreEffect, core);

        Particle finalParticle = Particle.WITCH;
        END_EFFECT.addEffect(endEffect, finalParticle);
    }

    public EldritchBlastSpell() {
        super("eldritch_blast");

        setAttribute(COST, 150);
        setAttribute(SPEED, 3d);
        setAttribute(DAMAGE, 6.0);
        setAttribute(RANGE, 50.0);
        setAttribute(IMPRECISION, 5d);
    }

    @Override
    public void configure(DynamicProjectileObject projectile, CastContext context) {
        projectile.setParticle(EFFECT);
        projectile.setEndEffects(END_EFFECT);
    }

    @Override
    public Particle getDefaultParticle() {
        return Particle.WITCH;
    }

    @Override
    public Component description() {
        return Component.text(
                "The most simple projectile spell that fires a blast of energy in the direction the caster is facing, dealing damage to anything hit."
        );
    }
}
