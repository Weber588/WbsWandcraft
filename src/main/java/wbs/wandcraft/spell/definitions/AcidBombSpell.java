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
import wbs.wandcraft.spell.definitions.extensions.DurationalSpell;
import wbs.wandcraft.spell.definitions.extensions.RadiusedSpell;
import wbs.wandcraft.spell.definitions.type.SpellType;
import wbs.wandcraft.spell.event.SpellTriggeredEvents;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AcidBombSpell extends SpellDefinition implements CustomProjectileSpell, DurationalSpell, DamageSpell, RadiusedSpell {
    private static final NormalParticleEffect BOMB_EFFECT = (NormalParticleEffect) new NormalParticleEffect()
            .setXYZ(0.4)
            .setAmount(0)
            .setData(new Particle.DustOptions(Color.fromRGB(156, 222, 98), 1f));
    private static final NormalParticleEffect EXPLODE_EFFECT = (NormalParticleEffect) new NormalParticleEffect()
            .setSpeed(0.2)
            .setAmount(60);

    private static final Map<PotionEffectType, Double> HIT_EFFECTS = Map.of(
            PotionEffectType.SLOWNESS, 1d,
            PotionEffectType.POISON, 1d,
            PotionEffectType.NAUSEA, 0.3
    );

    public AcidBombSpell() {
        super("acid_bomb");

        addSpellType(SpellType.NATURE);

        setAttribute(COST, 300);
        setAttribute(COOLDOWN, 10 * Ticks.TICKS_PER_SECOND);

        setAttribute(DAMAGE, 6d);
        setAttribute(RADIUS, 4d);
        setAttribute(DURATION, 12);

        setAttribute(GRAVITY, 0.25);
        setAttribute(SPEED, 3d);
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
            debug("Expired.");
            EXPLODE_EFFECT.play(Particle.SNEEZE, result);
            EXPLODE_EFFECT.play(Particle.TOTEM_OF_UNDYING, result);

            RadiusSelector<LivingEntity> selector = new RadiusSelector<>(LivingEntity.class);
            selector.setRange(instance.getAttribute(RADIUS));
            selector.exclude(context.player());

            List<LivingEntity> nearby = selector.select(result);

            int duration = instance.getAttribute(DURATION);

            List<PotionEffect> effects = new LinkedList<>();

            HIT_EFFECTS.forEach((type, multiplier) -> {
                PotionEffect effect = new PotionEffect(type, (int) Math.ceil(duration * multiplier), 0, false, true, true);
                effects.add(effect);
            });

            for (LivingEntity hit : nearby) {
                damageThen(hit, context, _ -> {
                    effects.forEach(hit::addPotionEffect);
                });
            }
        });
    }

    @Override
    public Particle getDefaultParticle() {
        return Particle.DUST;
    }
}
