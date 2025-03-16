package wbs.wandcraft.spell.definitions;

import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;
import wbs.wandcraft.objects.generics.DynamicProjectileObject;
import wbs.wandcraft.spell.definitions.extensions.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CustomProjectileSpell;
import wbs.wandcraft.spell.definitions.extensions.DamageSpell;
import wbs.wandcraft.spell.event.SpellTriggeredEvents;

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

        addAttribute(SPEED, 4.0);
        addAttribute(DAMAGE, 6.0);
    }

    @Override
    public void configure(DynamicProjectileObject projectile, CastContext context) {
        projectile.setParticle(EFFECT);
        projectile.setEndEffects(END_EFFECT);

        SpellInstance instance = context.instance();

        SpellTriggeredEvents.ON_HIT_TRIGGER.registerAnonymous(instance, (result) -> {
            Entity hitEntity = result.getHitEntity();
            double damage = instance.getAttribute(DAMAGE);

            if (hitEntity != null && damage > 0) {
                context.player().damage(damage, hitEntity);
            }
        });
    }

    @Override
    public Particle getDefaultParticle() {
        return Particle.WITCH;
    }
}
