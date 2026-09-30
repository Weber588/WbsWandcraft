package wbs.wandcraft.spell.dynamic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.papermc.paper.entity.TeleportFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsEventUtils;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.ParticleDataProvider;
import wbs.utils.util.pluginhooks.hooks.PacketEventsWrapper;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.context.CastingManager;
import wbs.wandcraft.cost.PlayerMana;
import wbs.wandcraft.events.SpellCastEvent;
import wbs.wandcraft.objects.MagicObjectManager;
import wbs.wandcraft.objects.generics.MagicObject;
import wbs.wandcraft.spell.SpellType;
import wbs.wandcraft.spell.SpellTypeModifiers;
import wbs.wandcraft.spell.attributes.attributable.DurationAttributable;
import wbs.wandcraft.spell.attributes.attributable.HealthAttributable;
import wbs.wandcraft.spell.attributes.attributable.RadiusAttributable;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.effect.SpellEffectDefinitions;
import wbs.wandcraft.spell.effect.SpellEffectInstance;
import wbs.wandcraft.spell.trigger.SpellTriggeredEvents;
import wbs.wandcraft.util.EffectUtils;

import java.util.*;

@NullMarked
public class DynamicSpellMagicCircle extends DynamicSpell implements CastableSpell, HealthAttributable, RadiusAttributable, DurationAttributable {
    public static final org.bukkit.NamespacedKey ENDER_CIRCLE_TP_TAG = WbsWandcraft.getKey("ender_circle_tp");
    public static SpellAspect MAGIC_CIRCLE = new FixedTypeSpellAspect("magic_circle", DynamicSpellMagicCircle::new);

    public DynamicSpellMagicCircle(@Nullable SpellType secondary) {
        super("magic_circle", SpellType.ARCANE, secondary);

        setAttribute(COST, 500);
        setAttribute(COOLDOWN, 60 * Ticks.TICKS_PER_SECOND);

        setAttribute(RADIUS, 1.5d);
        if (secondary == SpellType.ENDER) {
            setAttribute(DURATION, 15 * 60 * Ticks.TICKS_PER_SECOND);
        } else {
            setAttribute(DURATION, 15 * Ticks.TICKS_PER_SECOND);
        }
    }
    private static final int GLYPHS_PER_BLOCK = 3;
    private static final int MANA_PER_TICK = 3;

    @Override
    public void cast(CastContext context) {
        MagicCircleObject circle = new MagicCircleObject(context.player().getLocation(), context);

        if (requiresConcentration()) {
            circle.startConcentrating();
        }

        if (getSecondarySpellType() == SpellType.ENDER) {
            context.player().getPersistentDataContainer().set(ENDER_CIRCLE_TP_TAG, PersistentDataType.INTEGER, Bukkit.getCurrentTick());
        }

        circle.spawn();
    }

    @Override
    public boolean requiresConcentration() {
        if (getSecondarySpellType() == SpellType.ENDER) {
            return false;
        }
        return true;
    }

    @Override
    protected String rawDescription() {
        return "Create a circle of runes that regenerates mana and magic attacks.";
    }

    private class MagicCircleObject extends MagicObject {
        private final List<TextDisplay> displays = new LinkedList<>();
        private final double radius;
        private final RadiusSelector<LivingEntity> selector;
        private WbsEventUtils.EventHandlerMethod<SpellCastEvent> registeredEvent;

        public MagicCircleObject(Location location, CastContext context) {
            super(location.setRotation(0, 0), context);

            radius = context.instance().getAttribute(RADIUS);
            setMaxAge(context.instance().getAttribute(DURATION));

            selector = new RadiusSelector<>(LivingEntity.class)
                    .setRange(radius);
        }

        @Override
        protected void onSpawn() {
            Location location = getLocation();
            World world = location.getWorld();

            registeredEvent = WbsEventUtils.register(WbsWandcraft.getInstance(), SpellCastEvent.class, this::onCast);

            TextColor textColor = getTypeColours().getLast();

            Vector offset = new Vector(radius, 0, 0);

            int points = (int) (GLYPHS_PER_BLOCK * Math.PI * 2 * radius);
            double angleBetweenGlyphs = Math.TAU / points;

            location.setRotation(0, 0);

            float textScale = 1.5f;
            Vector3f scaleVector = new Vector3f(textScale, textScale, textScale);
            for (int i = 0; i < points; i++) {
                TextDisplay textDisplay = EffectUtils.getGlyphDisplay(
                        textColor,
                        location,
                        new Transformation(
                                offset.toVector3f(),
                                new AxisAngle4f((float) (i * angleBetweenGlyphs + (Math.PI / 2)), 0, 1, 0),
                                scaleVector,
                                new AxisAngle4f((float) -(Math.PI / 2), 1, 0, 0)
                        )
                );
                displays.add(textDisplay);

                offset.rotateAroundY(angleBetweenGlyphs);
            }

            PacketEventsWrapper.get().ifPresentOrElse(
                    pe -> {
                        world.getPlayersSeeingChunk(location.getChunk()).forEach(
                                player -> {
                                    displays.forEach(display -> {
                                        pe.showFakeEntity(display, player);
                                    });
                                });
                        },
                    () -> {
                        displays.forEach(world::addEntity);
                    }
            );
        }

        private void onCast(SpellCastEvent event) {
            if (event.getContext().player().getLocation().distance(getLocation()) > this.radius) {
                return;
            }

            SpellInstance instance = event.getContext().instance();
            SpellDefinition definition = instance.getDefinition();

            SpellType thisSpellType = getSecondarySpellType();
            if (thisSpellType == null) {
                thisSpellType = getPrimarySpellType();
            }
            if (definition.getPrimarySpellType() == thisSpellType) {
                Set<SpellAttributeModifier<?, ?>> modifiers = SpellTypeModifiers.getSpellTypeModifiers(thisSpellType);
                for (SpellAttributeModifier<?, ?> modifier : modifiers) {
                    modifier.modify(instance);
                }
            }
        }

        @Override
        protected boolean tick() {
            Particle ambientParticle = spellTypes.stream()
                    .map(SpellType::ambientParticle)
                    .filter(Objects::nonNull)
                    .toList()
                    .getLast();

            if (Bukkit.getCurrentTick() % 5 == 0) {
                ParticleDataProvider.playEffectSafely(
                        new NormalParticleEffect(),
                        getLocation().add(new Vector(radius / 2, Math.random() * 4, 0).rotateAroundY(Math.random() * Math.TAU)),
                        ambientParticle,
                        DynamicSpellMagicCircle.this
                );
            }

            List<LivingEntity> inCircle = selector.select(getLocation());

            for (LivingEntity target : inCircle) {
                context.runEffects(SpellTriggeredEvents.INDIRECT_TARGET_ENTITY_TRIGGER, target);
            }

            return DynamicSpellMagicCircle.this.requiresConcentration() && !CastingManager.isConcentrating(context.player(), context);
        }


        @Override
        protected void onRemove() {
            Location location = getLocation();
            World world = location.getWorld();

            world.getPlayersSeeingChunk(location.getChunk()).forEach(player -> {
                displays.forEach(display -> {

                    PacketEventsWrapper.get().ifPresentOrElse(pe -> {
                        pe.removeEntity(display, player);
                    }, display::remove);
                });
            });

            SpellCastEvent.getHandlerList().unregister(registeredEvent);
        }
    }

    @Override
    public Component displayName() {
        return super.displayName().color(getTypeColours().getLast());
    }

    @Override
    protected Multimap<SpellType, SpellEffectInstance<?>> typedEvents() {
        Multimap<SpellType, SpellEffectInstance<?>> events = HashMultimap.create();

        events.put(
                SpellType.ARCANE,
                SpellTriggeredEvents.INDIRECT_TARGET_ENTITY_TRIGGER.getAnonymousInstance(
                        ((context, instance, entity) -> {
                            switch (entity) {
                                case Player player -> new PlayerMana(player)
                                        .addMana(MANA_PER_TICK)
                                        .saveTo(player);
                                case Spellcaster spellcaster -> {
                                    if (spellcaster.getTarget() != null) {
                                        beginCasting(spellcaster);
                                    }
                                }
                                case Guardian guardian -> guardian.setLaserTicks(guardian.getLaserTicks() + 1);
                                case Vex vex -> vex.heal(1, EntityRegainHealthEvent.RegainReason.MAGIC_REGEN);
                                default -> {}
                            }
                        })
                )
        );

        events.put(
                SpellType.ENDER,
                SpellTriggeredEvents.INDIRECT_TARGET_ENTITY_TRIGGER.getAnonymousInstance(((context, instance, entity) -> {
                    int lastTPTick = entity.getPersistentDataContainer().getOrDefault(ENDER_CIRCLE_TP_TAG, PersistentDataType.INTEGER, 0);

                    // If entity has been out of the circle for less than 0.5 seconds (or never left), update the tag and stop.
                    // This way the value is always current until they're outside for long enough.
                    if (Bukkit.getCurrentTick() - lastTPTick < 0.5 * Ticks.TICKS_PER_SECOND) {
                        entity.getPersistentDataContainer().set(ENDER_CIRCLE_TP_TAG, PersistentDataType.INTEGER, Bukkit.getCurrentTick());
                        return;
                    }

                    List<MagicCircleObject> sortedEnderCircles = MagicObjectManager.getAllActive(MagicCircleObject.class)
                            .stream()
                            .filter(activeCircle ->
                                    activeCircle.getContext().instance().getDefinition().getSecondarySpellType() == SpellType.ENDER
                            ).sorted(Comparator.comparing(
                                    MagicObject::getLocation,
                                    Comparator.comparingDouble(loc -> loc.distance(entity.getLocation()))
                            )).toList();

                    if (sortedEnderCircles.size() > 1) {
                        MagicCircleObject target = sortedEnderCircles.get(1);
                        Location targetLoc = target.getLocation();

                        EffectUtils.playTeleportEffect(entity.getLocation());
                        Vector direction = entity.getLocation().getDirection();
                        entity.teleport(targetLoc.setDirection(direction), TeleportFlag.Relative.VELOCITY_ROTATION);
                        EffectUtils.playTeleportEffect(targetLoc);
                        
                        entity.getPersistentDataContainer().set(
                                ENDER_CIRCLE_TP_TAG,
                                PersistentDataType.INTEGER, 
                                Bukkit.getCurrentTick()
                        );
                    }
                }))
        );

        events.put(
                SpellType.NATURE,
                SpellTriggeredEvents.INDIRECT_TARGET_ENTITY_TRIGGER.getInstance(SpellEffectDefinitions.HEAL)
        );

        return events;
    }

    private static void beginCasting(Spellcaster spellcaster) {
        List<Spellcaster.Spell> spells;
        if (spellcaster.getSpell() == Spellcaster.Spell.NONE) {
            if (spellcaster instanceof Evoker) {
                spells = List.of(
                        Spellcaster.Spell.FANGS,
                        Spellcaster.Spell.SUMMON_VEX,
                        Spellcaster.Spell.WOLOLO
                );
            } else if (spellcaster instanceof Illusioner) {
                spells = List.of(
                        Spellcaster.Spell.BLINDNESS,
                        Spellcaster.Spell.DISAPPEAR
                );
            } else {
                return;
            }
            spellcaster.setSpell(WbsCollectionUtil.getRandom(spells));
        }
    }
}
