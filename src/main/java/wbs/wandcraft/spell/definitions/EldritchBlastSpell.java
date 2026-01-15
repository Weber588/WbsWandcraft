package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.Particle;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.objects.generics.DynamicProjectileObject;
import wbs.wandcraft.spell.definitions.extensions.CustomProjectileSpell;
import wbs.wandcraft.spell.definitions.extensions.DamageSpell;

import static wbs.wandcraft.spell.definitions.type.SpellType.ARCANE;
import static wbs.wandcraft.spell.definitions.type.SpellType.VOID;

public class EldritchBlastSpell extends SpellDefinition implements CustomProjectileSpell, DamageSpell {
    private static final WbsParticleGroup EFFECT = new WbsParticleGroup();
    private static final WbsParticleGroup END_EFFECT = new WbsParticleGroup();

    static {
        NormalParticleEffect effect = new NormalParticleEffect();
        double size = 0.1;
        int particleAmount = (int) (size * 25);
        effect.setAmount(particleAmount);
        effect.setXYZ(size);
        Particle.DustOptions data = new Particle.DustOptions(VOID.mulColor(1.5), 0.8F);
        effect.setData(data);

        NormalParticleEffect coreEffect = new NormalParticleEffect();
        coreEffect.setAmount((int) Math.ceil(particleAmount / 3.0));
        coreEffect.setXYZ(0.1);

        NormalParticleEffect endEffect = new NormalParticleEffect();
        endEffect.setAmount(150);
        endEffect.setXYZ(size);
        endEffect.setSpeed(0.1);

        NormalParticleEffect endEffect2 = new NormalParticleEffect();
        endEffect2.setAmount(10);
        endEffect2.setXYZ(0);
        endEffect2.setSpeed(0.1);

        Particle particle = Particle.DUST;
        EFFECT.addEffect(effect, particle);
        Particle core = Particle.SMOKE;
        EFFECT.addEffect(coreEffect, core);

        END_EFFECT.addEffect(endEffect, Particle.SMOKE);
        END_EFFECT.addEffect(endEffect2, Particle.SQUID_INK);
    }

    public EldritchBlastSpell() {
        super("eldritch_blast");

        addSpellType(VOID);
        addSpellType(ARCANE);

        setAttribute(COST, 150);
        setAttribute(COOLDOWN, 1 * Ticks.TICKS_PER_SECOND);

        setAttribute(SPEED, 3d);
        setAttribute(DAMAGE, 6.0);
        setAttribute(RANGE, 50.0);
        setAttribute(IMPRECISION, 0d);
        setAttribute(GRAVITY, 0d);
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
    public String rawDescription() {
        return "A simple projectile spell that fires a blast of energy in the direction the caster is facing, dealing damage to anything hit.";
    }
}
