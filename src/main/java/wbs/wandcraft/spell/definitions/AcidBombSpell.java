package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.objects.generics.DynamicProjectileObject;
import wbs.wandcraft.spell.definitions.extensions.CustomProjectileSpell;
import wbs.wandcraft.spell.definitions.extensions.DamageSpell;
import wbs.wandcraft.spell.definitions.extensions.RadiusedSpell;
import wbs.wandcraft.spell.definitions.type.SpellType;
import wbs.wandcraft.spell.event.SpellTriggeredEvents;

import java.util.List;

public class AcidBombSpell extends SpellDefinition implements CustomProjectileSpell, DamageSpell, RadiusedSpell {
    private static final NormalParticleEffect BOMB_EFFECT = (NormalParticleEffect) new NormalParticleEffect()
            .setXYZ(0.4)
            .setAmount(0)
            .setData(new Particle.DustOptions(Color.fromRGB(156, 222, 98), 1f));
    private static final NormalParticleEffect EXPLODE_EFFECT = (NormalParticleEffect) new NormalParticleEffect()
            .setSpeed(0.2)
            .setAmount(60);

    private static final List<PotionEffect> HIT_EFFECTS = List.of(
            new PotionEffect(PotionEffectType.SLOWNESS, 12, 1),
            new PotionEffect(PotionEffectType.POISON, 12, 0),
            new PotionEffect(PotionEffectType.NAUSEA, 4, 0)
    );

    public AcidBombSpell() {
        super("acid_bomb");

        addSpellType(SpellType.NATURE);

        setAttribute(COST, 1000);
        setAttribute(COOLDOWN, 30 * Ticks.TICKS_PER_SECOND);

        setAttribute(DAMAGE, 6d);
        setAttribute(RADIUS, 4d);

        setAttribute(GRAVITY, 0.25);
        setAttribute(SPEED, 0.1d);
        setAttribute(IMPRECISION, 5d);
    }

    @Override
    public String rawDescription() {
        return "Fires a blob of acid that damages and poisons mobs in an area where it hits";
    }

    @Override
    public void configure(DynamicProjectileObject projectile, CastContext context) {
        projectile.setParticle(new WbsParticleGroup().addEffect(BOMB_EFFECT, Particle.DUST));

        SpellInstance instance = context.instance();

        SpellTriggeredEvents.OBJECT_EXPIRE_TRIGGER.registerAnonymous(instance, (result) -> {
            EXPLODE_EFFECT.play(Particle.SNEEZE, result);
            EXPLODE_EFFECT.play(Particle.TOTEM_OF_UNDYING, result);

            RadiusSelector<LivingEntity> selector = new RadiusSelector<>(LivingEntity.class);
            selector.setRange(instance.getAttribute(RADIUS));
            selector.exclude(context.player());

            List<LivingEntity> nearby = selector.select(result);

            for (LivingEntity hit : nearby) {
                damageThen(hit, context, damageable -> {
                    HIT_EFFECTS.forEach(hit::addPotionEffect);
                });
            }
        });
    }

    @Override
    public Particle getDefaultParticle() {
        return Particle.DUST;
    }
}
