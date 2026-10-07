package wbs.wandcraft.spell.dynamic;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.RingParticleEffect;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.MagicDomain;
import wbs.wandcraft.spell.definitions.extensions.ContinuousCastableSpell;

@NullMarked
public class DynamicSpellBarrage extends DynamicSpellProjectile implements ContinuousCastableSpell {
    private static final double RING_DISTANCE_FROM_EYE_LOC = 1;
    private static final double MIN_IMPRECISION_FACTOR = 3;
    public static final int ROTATION_SPEED = 30 / Ticks.TICKS_PER_SECOND;

    public static SpellArchetype BARRAGE = new GenericSpellArchetype(
            "barrage",
            Component.text("Fires a series of projectiles in the direction the caster is facing."),
            DynamicSpellBarrage::new
    );

    public DynamicSpellBarrage(MagicDomain primary, @Nullable MagicDomain secondary) {
        super(BARRAGE, primary, secondary);

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
}
