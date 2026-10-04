package wbs.wandcraft.spell.dynamic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.Ticks;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.particles.ParticleDataProvider;
import wbs.utils.util.particles.RingParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.cost.CostType;
import wbs.wandcraft.spell.SpellType;
import wbs.wandcraft.spell.attributes.attributable.DirectionAttributable;
import wbs.wandcraft.spell.attributes.attributable.RangeAttributable;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.extensions.ContinuousCastableSpell;
import wbs.wandcraft.spell.effect.SpellEffectDefinitions;
import wbs.wandcraft.spell.effect.SpellEffectInstance;
import wbs.wandcraft.spell.trigger.SpellTriggeredEvents;

import java.util.LinkedList;
import java.util.List;

@NullMarked
public class DynamicConeSpell extends DynamicSpell implements ContinuousCastableSpell, DirectionAttributable, RangeAttributable {
    public static SpellAspect CONE_ASPECT = new GenericSpellAspect(
            "cone",
            Component.text("Emits a blast of energy in a cone in front of the caster."),
            DynamicConeSpell::new);
    private final WbsParticleGroup particleGroup;

    public DynamicConeSpell(SpellType primary, @Nullable SpellType secondary) {
        super(CONE_ASPECT, primary, secondary);

        particleGroup = getParticleGroup(
                new RingParticleEffect()
                        .setRadius(0.01)
                        .setVariation(0.03)
                        .setAmount(7),
                new RingParticleEffect()
                        .setRadius(0.01)
                        .setVariation(0.03)
                        .setAmount(2),
                true
        ).setPlayFunction(((effect, location, particle) -> {
            effect.build();
            ParticleDataProvider.playEffectSafely(effect, location, particle, this);
        }));

        setAttribute(COST, 100);
        setAttribute(COOLDOWN, 30 * Ticks.TICKS_PER_SECOND);

        setAttribute(IMPRECISION, 20d);

        setAttribute(FIXED_DURATION, 3 * Ticks.TICKS_PER_SECOND);
        setAttribute(MAX_DURATION, 10 * Ticks.TICKS_PER_SECOND);
        setAttribute(RANGE, 5d);
        setAttribute(COST_PER_TICK, 5);
    }

    @Override
    protected String rawDescription() {
        return "Continuously affect a cone in front of you.";
    }

    @Override
    public void tick(CastContext context, int tick, int ticksLeft) {
        SpellInstance instance = context.instance();
        Player player = context.getOnlinePlayer();
        if (player == null || !player.isOnline()) {
            return;
        }

        Vector direction = getDirection(context, player, 0.2);
        double range = instance.getAttribute(RANGE);

        Location location = player.getEyeLocation();

        double spellTypeSpeedModifier = getPrimarySpellType().velocityParticleModifier();

        particleGroup.effects().forEach((effect, particle) -> {
            if (effect instanceof RingParticleEffect ring) {
                ring.setRotation(Math.random() * 360)
                        .setAbout(direction)
                        .setDirection(direction)
                        .setSpeed((range + 1) / 4 * spellTypeSpeedModifier);
            }
        });

        particleGroup.buildAndPlay(location.clone().add(direction).add(0, -0.15, 0));;

        List<Entity> hitEntities = new LinkedList<>();

        double raySize = 0.3;

        RayTraceResult result;
        do {
            result = location.getWorld().rayTrace(
                    location,
                    direction,
                    range,
                    FluidCollisionMode.NEVER,
                    true,
                    raySize,
                    entity -> !entity.equals(player) && !hitEntities.contains(entity)
            );

            if (result != null) {
                Entity hitEntity = result.getHitEntity();
                if (hitEntity != null) {
                    hitEntities.add(hitEntity);
                }

                context.runEffects(SpellTriggeredEvents.ON_HIT_TRIGGER, result);
            }
        } while (result != null && result.getHitEntity() != null);
    }

    @Override
    protected Multimap<SpellType, SpellEffectInstance<?>> typedEvents() {
        HashMultimap<SpellType, SpellEffectInstance<?>> typedEvents = HashMultimap.create();

        double chance = 0.05;
        typedEvents.put(
                SpellType.NETHER,
                // Damage is already handled by attributes
                SpellTriggeredEvents.ON_HIT_TRIGGER.getInstance(SpellEffectDefinitions.IGNITE).chance(chance)
        );

        typedEvents.put(
                SpellType.ENDER,
                SpellTriggeredEvents.ON_HIT_TRIGGER.getInstance(SpellEffectDefinitions.RANDOM_TELEPORT).chance(chance)
        );

        typedEvents.put(
                SpellType.SCULK,
                SpellTriggeredEvents.ON_HIT_TRIGGER.getAnonymousInstance(((context, effectInstance, result) -> {
                    Entity hitEntity = result.getHitEntity();
                    if (hitEntity instanceof LivingEntity entity) {
                        PotionEffect effect = WbsCollectionUtil.getRandom(CostType.FATIGUE_EFFECTS);
                        entity.addPotionEffect(effect);
                    }
                })).chance(chance)
        );

        typedEvents.putAll(
                SpellType.NATURE,
                List.of(
                        SpellTriggeredEvents.ON_HIT_TRIGGER.getInstance(SpellEffectDefinitions.HEAL).chance(chance),
                        SpellTriggeredEvents.ON_HIT_TRIGGER.getInstance(SpellEffectDefinitions.GROW).chance(chance)
                )
        );

        return typedEvents;
    }

    @Override
    public float getFloat(Particle particle, @Nullable Location location) {
        return 1;
    }
}
