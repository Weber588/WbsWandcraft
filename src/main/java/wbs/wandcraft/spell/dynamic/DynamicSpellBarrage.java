package wbs.wandcraft.spell.dynamic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.RingParticleEffect;
import wbs.utils.util.particles.WbsParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;
import wbs.utils.util.particles.entity.DisplayParticleBuilder;
import wbs.utils.util.particles.entity.EntityParticle;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.cost.CostType;
import wbs.wandcraft.objects.generics.DynamicProjectileObject;
import wbs.wandcraft.spell.SpellType;
import wbs.wandcraft.spell.definitions.extensions.ContinuousCastableSpell;
import wbs.wandcraft.spell.effect.SpellEffectDefinitions;
import wbs.wandcraft.spell.effect.SpellEffectInstance;
import wbs.wandcraft.spell.trigger.SpellTriggeredEvents;

import java.util.List;
import java.util.Set;

@NullMarked
public class DynamicSpellBarrage extends DynamicSpellProjectile implements ContinuousCastableSpell {
    private static final double RING_DISTANCE_FROM_EYE_LOC = 1;
    private static final double MIN_IMPRECISION_FACTOR = 3;
    public static final int ROTATION_SPEED = 30 / Ticks.TICKS_PER_SECOND;

    public static SpellAspect BARRAGE_ASPECT = new GenericSpellAspect(
            "barrage",
            Component.text("Fires a series of projectiles in the direction the caster is facing."),
            DynamicSpellBarrage::new
    );

    private static final Set<Material> NATURE_BLOCKS = Set.of(
            Material.MOSS_BLOCK,
            Material.STONE,
            Material.COBBLESTONE,
            Material.MOSSY_COBBLESTONE
    );

    public DynamicSpellBarrage(SpellType primary, @Nullable SpellType secondary) {
        super(BARRAGE_ASPECT, primary, secondary);

        setAttribute(COST_PER_TICK, 0);
        setAttribute(FIXED_DURATION, 5 * Ticks.TICKS_PER_SECOND);
        setAttribute(MAX_DURATION, 10 * Ticks.TICKS_PER_SECOND);

        setAttribute(IMPRECISION, 30d);
    }

    @Override
    public void cast(CastContext context) {
        ContinuousCastableSpell.super.cast(context);
    }

    @Override
    public void tick(CastContext context, int tick, int ticksLeft) {
        RingParticleEffect ringEffect = new RingParticleEffect();

        double maxPrecisionTick = (double) context.instance().getAttribute(MAX_DURATION) / 2;
        double angle = getCurrentImprecision(context, tick, maxPrecisionTick);

        Vector facingVector = WbsEntityUtil.getFacingVector(context.player(), RING_DISTANCE_FROM_EYE_LOC);
        double radius = Math.tan(Math.toRadians(angle)) / RING_DISTANCE_FROM_EYE_LOC;
        ringEffect.setAbout(facingVector);
        ringEffect.setAmount(9);
        ringEffect.setRadius(radius);
        ringEffect.setRotation(-(Bukkit.getCurrentTick() % ((double) 360 / ROTATION_SPEED)) * ROTATION_SPEED);
        Particle.DustOptions ringData = new Particle.DustOptions(Color.fromRGB(getTypeColours().getFirst().value()), (float) Math.max(radius/2, 0.2));
        ringEffect.setData(ringData);

        ringEffect.buildAndPlay(Particle.DUST, context.player().getEyeLocation().add(facingVector));

        if (tick > maxPrecisionTick) {
            WbsWandcraft.getInstance().buildMessageNoPrefix("Maximum accuracy!")
                    .build()
                    .sendActionBar(context.player());
        }
    }

    private static double getCurrentImprecision(CastContext context, int tick, double maxPrecisionTick) {
        double angle = context.instance().getAttribute(IMPRECISION);

        if (tick > maxPrecisionTick) {
            return angle / MIN_IMPRECISION_FACTOR;
        } else {
            return WbsMath.lerp(angle, angle / MIN_IMPRECISION_FACTOR, tick / maxPrecisionTick);
        }
    }

    @Override
    public void onStopCasting(CastContext context, int tick, int ticksLeft) {
        double angle = getCurrentImprecision(context, tick, (double) context.instance().getAttribute(MAX_DURATION) / 2);

        context.instance().setAttribute(IMPRECISION, angle);
        CastContext updatedContext = new CastContext(context.player(),
                context.instance(),
                context.wand(),
                context.slot(),
                context.player().getEyeLocation(),
                context,
                null);
        WbsWandcraft.getInstance().runTimerNTimes(_ -> {
            super.cast(updatedContext);
        }, 3, 0, 3);
    }

    @Override
    public void configure(DynamicProjectileObject projectile, CastContext context) {
        if (getPrimarySpellType() == SpellType.NATURE) {
            float size = (float) (double) context.instance().getAttribute(SIZE) * 1.7f;
            float speed  = (float) (double) context.instance().getAttribute(SPEED);

            Vector3fc axis = new Vector3f(1, 0, (float) Math.random() * 2 - 1);

            EntityParticle<BlockDisplay> particle = new DisplayParticleBuilder<>(BlockDisplay.class)
                    .rotationPivot(new Vector(-size / 2, -size / 2, -size / 2))
                    .setAngularVelocity(Vector.fromJOML(axis).normalize().multiply(speed / 2))
                    .setInterpolationDuration((int) Math.ceil(Ticks.TICKS_PER_SECOND / Bukkit.getServerTickManager().getTickRate()))
                    .editTransformation(t -> {
                        t.translate(-size / 2, -size / 2, -size / 2)
                                .scale(size);
                    })
                    .configure(display -> {
                        display.setBlock(WbsCollectionUtil.getRandom(NATURE_BLOCKS).createBlockData());
                    }).playParticle(context.location());

            projectile.follower(particle.getEntity());
            projectile.setTickEffects(null);
        } else {
            WbsParticleGroup particleGroup = getParticleGroup(
                    buildParticleEffect(projectile).setAmount(2),
                    buildParticleEffect(projectile).setAmount(1)
            );

            projectile.setTickEffects(particleGroup);
        }
        projectile.playEffectsOnTick(false);
    }

    private static WbsParticleEffect buildParticleEffect(DynamicProjectileObject projectile) {
        return new NormalParticleEffect().setXYZ(projectile.getHitBoxSize() / 3);
    }

    @Override
    protected Multimap<SpellType, SpellEffectInstance<?>> typedEvents() {
        HashMultimap<SpellType, SpellEffectInstance<?>> typedEvents = HashMultimap.create();

        typedEvents.put(
                SpellType.NETHER,
                // Damage is already handled by attributes
                SpellTriggeredEvents.ON_HIT_TRIGGER.getInstance(SpellEffectDefinitions.IGNITE)
        );

        typedEvents.put(
                SpellType.ENDER,
                SpellTriggeredEvents.ON_HIT_TRIGGER.getInstance(SpellEffectDefinitions.RANDOM_TELEPORT)
        );

        typedEvents.put(
                SpellType.SCULK,
                SpellTriggeredEvents.ON_HIT_TRIGGER.getAnonymousInstance(((context, effectInstance, result) -> {
                    Entity hitEntity = result.getHitEntity();
                    if (hitEntity instanceof LivingEntity entity) {
                        PotionEffect effect = WbsCollectionUtil.getRandom(CostType.FATIGUE_EFFECTS);
                        entity.addPotionEffect(effect);
                    }
                }))
        );

        typedEvents.putAll(
                SpellType.NATURE,
                List.of(
                        SpellTriggeredEvents.ON_HIT_TRIGGER.getInstance(SpellEffectDefinitions.HEAL),
                        SpellTriggeredEvents.ON_HIT_TRIGGER.getInstance(SpellEffectDefinitions.GROW)
                )
        );

        return typedEvents;
    }
}
