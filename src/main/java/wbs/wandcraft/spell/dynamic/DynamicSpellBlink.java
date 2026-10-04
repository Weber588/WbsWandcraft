package wbs.wandcraft.spell.dynamic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.ParticleDataProvider;
import wbs.utils.util.particles.WbsParticleGroup;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.cost.CostType;
import wbs.wandcraft.spell.SpellType;
import wbs.wandcraft.spell.attributes.attributable.DirectionAttributable;
import wbs.wandcraft.spell.attributes.attributable.RangeAttributable;
import wbs.wandcraft.spell.attributes.attributable.SpeedAttributable;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.effect.SpellEffectDefinitions;
import wbs.wandcraft.spell.effect.SpellEffectInstance;
import wbs.wandcraft.spell.trigger.SpellTriggeredEvents;

import java.util.List;

@NullMarked
public class DynamicSpellBlink extends DynamicSpell implements CastableSpell, RangeAttributable, SpeedAttributable, DirectionAttributable {
    public static final SpellType PRIMARY = SpellType.ENDER;
    public static SpellAspect BLINK_ASPECT = new FixedTypeSpellAspect(
            "blink", PRIMARY,
            Component.text("Teleport a short distance in the direction the caster is facing."),
            DynamicSpellBlink::new
    );
    private final WbsParticleGroup particleGroup;

    public DynamicSpellBlink(@Nullable SpellType secondary) {
        super("blink", PRIMARY, secondary);

        particleGroup = getParticleGroup(
                new NormalParticleEffect().setXYZ(0.6).setY(1).setAmount(250)
        ).setPlayFunction(((effect, location, particle) -> {
            effect.build();
            ParticleDataProvider.playEffectSafely(effect, location, particle, this);
        }));

        setAttribute(RANGE, 8.0);
        setAttribute(IMPRECISION, 5d);
        setAttribute(SPEED, 0.8);
    }

    @Override
    public void cast(CastContext context) {
        Player player = context.player();
        Location loc = player.getLocation();

        World world = loc.getWorld();
        particleGroup.play(loc.clone().add(0, 1, 0));
        world.spawnParticle(Particle.WITCH, loc, 100, 0.3, 0.5, 0.3, 0.2);

        double range = context.instance().getAttribute(RANGE);
        Vector direction = getDirection(context, range);
        // TODO: Change for safety works for different types
        Block tpLocation = WbsEntityUtil.getSafeLocation(player, context.location().add(direction), range);

        if (tpLocation != null) {
            // TODO: Create new triggers & configure by type
            player.teleport(tpLocation.getLocation().setDirection(WbsEntityUtil.getFacingVector(player)));
            loc = player.getLocation();

            particleGroup.play(loc.clone().add(0, 1, 0));
            world.spawnParticle(Particle.WITCH, loc, 100, 0.6, 0.5, 0.3, 0.2);

            // TODO: Add cast sounds to SpellDefinition
            // Need to do it after teleporting or it gets cut off for the user
            // getCastSound().play(loc);
            WbsEntityUtil.push(player, context.instance().getAttribute(SPEED));
        } else {
            WbsWandcraft.getInstance().sendActionBar("No safe space found!", player);
        }
    }

    @Override
    protected Multimap<SpellType, SpellEffectInstance<?>> typedEvents() {
        HashMultimap<SpellType, SpellEffectInstance<?>> typedEvents = HashMultimap.create();

        // TODO: Update these to make sense for blink lol
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

    @Override
    protected String getTextureKeyValue() {
        return "blink";
    }

    @Override
    public Component displayName() {
        return super.displayName().color(getTypeColours().getLast());
    }
}
