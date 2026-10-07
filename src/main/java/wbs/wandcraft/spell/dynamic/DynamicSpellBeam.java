package wbs.wandcraft.spell.dynamic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.papermc.paper.raytracing.RayTraceTarget;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.particles.entity.DisplayParticle;
import wbs.utils.util.particles.entity.TextDisplayParticleBuilder;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.context.CastingManager;
import wbs.wandcraft.cost.CostType;
import wbs.wandcraft.spell.MagicDomain;
import wbs.wandcraft.spell.attributes.attributable.DirectionAttributable;
import wbs.wandcraft.spell.attributes.attributable.RadiusAttributable;
import wbs.wandcraft.spell.attributes.attributable.RangeAttributable;
import wbs.wandcraft.spell.definitions.extensions.ContinuousCastableSpell;
import wbs.wandcraft.spell.effect.SpellEffectDefinitions;
import wbs.wandcraft.spell.effect.SpellEffectInstance;
import wbs.wandcraft.spell.trigger.SpellTriggeredEvents;
import wbs.wandcraft.util.EffectUtils;

import java.util.LinkedList;
import java.util.List;

@NullMarked
public class DynamicSpellBeam extends DynamicSpell implements ContinuousCastableSpell, RadiusAttributable, DirectionAttributable, RangeAttributable {
    private static final Vector3f DEFAULT_FACING_DIR = new Vector3f(0, 0, 1);
    private static final Vector3f UP = new Vector3f(0, 1, 0);
    private static final Vector3f HORIZONTAL_PERP = new Vector3f(DEFAULT_FACING_DIR).cross(UP).normalize();

    public static SpellArchetype BEAM = new GenericSpellArchetype(
            "beam",
            Component.text("Project a ray in the direction the caster is facing, instantly affecting everything in the path."),
            DynamicSpellBeam::new
    );

    public DynamicSpellBeam(MagicDomain primary, @Nullable MagicDomain secondary) {
        super(BEAM, primary, secondary);

        setAttribute(RADIUS, 0.4d);
        setAttribute(IMPRECISION, 0d);
    }

    @Override
    public void onStartCasting(CastContext context) {
        double radius = context.instance().getAttribute(RADIUS);
        double range = context.instance().getAttribute(RANGE);

        Vector direction = getDirection(context, context.player(), 1);

        boolean isContinuousCast = isContinuousCast(context.player());
        int duration;
        if (isContinuousCast) {
            duration = context.instance().getAttribute(MAX_DURATION);
        } else {
            duration = context.instance().getAttribute(FIXED_DURATION);
        }

        Location startLoc = context.location();

        Vector offsetToWand = EffectUtils.getOffsetToWand(context);
        startLoc.add(offsetToWand);
        startLoc.setDirection(Vector.fromJOML(DEFAULT_FACING_DIR));

        Vector hitPos = getBeamTarget(direction, radius, range, context.location());
        Vector offset = hitPos.clone().subtract(startLoc.toVector());

        TextDisplayParticleBuilder builder = (TextDisplayParticleBuilder) new TextDisplayParticleBuilder()
                .editTransformation(matrix -> {
                    drawLine(matrix, offset, (float) radius, 0);
                })
                .configure(display -> {
                    display.setBillboard(Display.Billboard.FIXED);
                    display.setBackgroundColor(getPrimaryDomain().color());
                    display.setInterpolationDelay(0);
                    display.setInterpolationDuration(20);
                    display.setTeleportDuration(1);
                })
                .setMaxAge(duration);

        List<DisplayParticle<TextDisplay>> particles = new LinkedList<>();
        particles.add((DisplayParticle<TextDisplay>) builder.playParticle(startLoc));

        for (int i = 0; i < 3; i++) {
            particles.add((DisplayParticle<TextDisplay>) builder.editTransformation(matrix -> {
                matrix.rotateY((float) Math.PI / 2);
            }).playParticle(startLoc));
        }

        WbsWandcraft.getInstance().runTimer(runnable -> {
            Player onlinePlayer = context.getOnlinePlayer();

            if (onlinePlayer == null || !CastingManager.isCasting(onlinePlayer, context)) {
                runnable.cancel();
                particles.forEach(particle -> particle.getEntity().remove());
                return;
            }

            int i = 0;
            for (DisplayParticle<TextDisplay> particle : particles) {
                TextDisplay entity = particle.getEntity();
                if (!entity.isValid()) {
                    runnable.cancel();
                    break;
                }
                Vector newDirection = getDirection(context, onlinePlayer, range);
                Location newLocation = onlinePlayer.getEyeLocation();

                Vector newHitPos = getBeamTarget(newDirection, radius, range, newLocation);
                Vector newOffsetToWand = EffectUtils.getOffsetToWand(context, onlinePlayer);
                Location newStartLoc = newLocation.add(newOffsetToWand);
                Vector newOffset = newHitPos.clone()
                        .subtract(newStartLoc.toVector());

                particle.teleport(newStartLoc.setDirection(Vector.fromJOML(DEFAULT_FACING_DIR)));

                float angle = i * (float) Math.PI / 2;
                particle.editTransformation(matrix -> {
                    drawLine(matrix, newOffset, (float) radius, angle);
                });

                i++;

//                    double alpha = WbsMath.lerp(255, 0, 1 - (double) (duration - entity.getTicksLived()) / duration);
//                    entity.setBackgroundColor(getPrimaryDomain().color().setAlpha((int) alpha));
            }
        }, 0, 1);
    }

    private static Vector getBeamTarget(Vector direction, double radius, double range, Location location) {
        RayTraceResult result = location.getWorld().rayTrace(builder ->
                builder.start(location)
                        .direction(direction)
                        .ignorePassableBlocks(true)
                        .raySize(radius)
                        .maxDistance(range)
                        .targets(RayTraceTarget.BLOCK)
        );

        Vector hitPos;
        if (result != null) {
            hitPos = result.getHitPosition();
        } else {
            hitPos = location.toVector().add(direction);
        }
        return hitPos;
    }

    private static void drawLine(Matrix4f matrix, Vector offset, float radius, float angle) {
        matrix.identity().rotationTowards(offset.toVector3f(), UP)
                .rotate((float) (Math.PI / 2), HORIZONTAL_PERP)
                .scale(
                        radius / TextDisplayParticleBuilder.DEFAULT_TEXT_DISPLAY_WIDTH,
                        (float) offset.length() / TextDisplayParticleBuilder.DEFAULT_TEXT_DISPLAY_HEIGHT,
                        radius / TextDisplayParticleBuilder.DEFAULT_TEXT_DISPLAY_WIDTH
                )
                .translate(0, -TextDisplayParticleBuilder.DEFAULT_TEXT_DISPLAY_HEIGHT, 0)
                .rotateY(angle)
        ;
    }

    @Override
    public void tick(CastContext context, int tick, int ticksLeft) {

    }

    @Override
    protected Multimap<MagicDomain, SpellEffectInstance<?>> typedEvents() {
        HashMultimap<MagicDomain, SpellEffectInstance<?>> typedEvents = HashMultimap.create();

        typedEvents.put(
                MagicDomain.NETHER,
                // Damage is already handled by attributes
                SpellTriggeredEvents.ON_HIT_TRIGGER.getInstance(SpellEffectDefinitions.IGNITE)
        );

        typedEvents.put(
                MagicDomain.ENDER,
                SpellTriggeredEvents.ON_HIT_TRIGGER.getInstance(SpellEffectDefinitions.RANDOM_TELEPORT)
        );

        typedEvents.put(
                MagicDomain.SCULK,
                SpellTriggeredEvents.ON_HIT_TRIGGER.getAnonymousInstance(((context, effectInstance, result) -> {
                    Entity hitEntity = result.getHitEntity();
                    if (hitEntity instanceof LivingEntity entity) {
                        PotionEffect effect = WbsCollectionUtil.getRandom(CostType.FATIGUE_EFFECTS);
                        entity.addPotionEffect(effect);
                    }
                }))
        );

        typedEvents.putAll(
                MagicDomain.NATURE,
                List.of(
                        SpellTriggeredEvents.ON_HIT_TRIGGER.getInstance(SpellEffectDefinitions.HEAL),
                        SpellTriggeredEvents.ON_HIT_TRIGGER.getInstance(SpellEffectDefinitions.GROW)
                )
        );

        return typedEvents;
    }
}
