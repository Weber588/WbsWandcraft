package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.objects.generics.DynamicProjectileObject;
import wbs.wandcraft.spell.attributes.attributable.DamageAttributable;
import wbs.wandcraft.spell.attributes.attributable.DurationAttributable;
import wbs.wandcraft.spell.attributes.attributable.RadiusAttributable;
import wbs.wandcraft.spell.definitions.extensions.CustomProjectileSpell;
import wbs.wandcraft.spell.SpellType;
import wbs.wandcraft.spell.trigger.SpellTriggeredEvents;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@NullMarked
public class AcidBombSpell extends SpellDefinition implements CustomProjectileSpell, DurationAttributable, DamageAttributable, RadiusAttributable {
    private static final NormalParticleEffect BOMB_EFFECT = (NormalParticleEffect) new NormalParticleEffect()
            .setXYZ(0.2)
            .setAmount(2)
            .setData(new Particle.DustOptions(Color.fromRGB(156, 222, 98), 1f));
    private static final NormalParticleEffect EXPLODE_EFFECT = (NormalParticleEffect) new NormalParticleEffect()
            .setSpeed(0.75)
            .setAmount(60);
    private static final NormalParticleEffect EXPLODE_EFFECT_2 = (NormalParticleEffect) new NormalParticleEffect()
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
    protected String rawDescription() {
        return "Fires a blob of acid that damages and poisons mobs in an area where it hits";
    }

    @Override
    public void configure(DynamicProjectileObject projectile, CastContext context) {
        SpellInstance instance = context.instance();
        projectile.setTickEffects(new WbsParticleGroup().addEffect(
                BOMB_EFFECT.clone()
                        .setXYZ(instance.getAttribute(SIZE) / 3),
                Particle.DUST)
        );

        SpellTriggeredEvents.OBJECT_EXPIRE_TRIGGER.registerAnonymous(instance, (expiringObject) -> {
            Location location = expiringObject.getLocation();
            EXPLODE_EFFECT.play(Particle.TOTEM_OF_UNDYING, location);
            EXPLODE_EFFECT_2.play(Particle.SNEEZE, location);

            RadiusSelector<LivingEntity> selector = new RadiusSelector<>(LivingEntity.class);
            selector.setRange(instance.getAttribute(RADIUS));
            selector.exclude(context.player());

            List<LivingEntity> nearby = selector.select(location);

            int duration = instance.getAttribute(DURATION);

            List<PotionEffect> effects = new LinkedList<>();

            HIT_EFFECTS.forEach((type, multiplier) -> {
                PotionEffect effect = new PotionEffect(type, (int) Math.ceil(duration * multiplier), 0, false, true, true);
                effects.add(effect);
            });

            for (LivingEntity hit : nearby) {
                damageThen(hit, context, _ -> {
                    effects.forEach(hit::addPotionEffect);
                    context.runEffects(SpellTriggeredEvents.INDIRECT_TARGET_ENTITY_TRIGGER, hit);
                });
            }
        });
    }

    @Override
    public Color getColor(Particle particle, @Nullable Location location) {
        return Color.fromRGB(156, 222, 98);
    }

    @Override
    public Particle getDefaultParticle() {
        return Particle.DUST;
    }
}
