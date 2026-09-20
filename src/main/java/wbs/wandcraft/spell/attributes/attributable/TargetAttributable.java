package wbs.wandcraft.spell.attributes.attributable;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.tags.EntityTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Registry;
import org.bukkit.entity.*;
import wbs.utils.util.WbsRegistryUtil;
import wbs.utils.util.entities.selector.LineOfSightSelector;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.utils.util.string.WbsStrings;
import wbs.wandcraft.AttributeDataType;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.SpellType;
import wbs.wandcraft.spell.attributes.*;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.trigger.SpellTriggeredEvent;
import wbs.wandcraft.spell.trigger.SpellTriggeredEvents;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public interface TargetAttributable<T extends Entity> extends AttributeHolder {
    SpellAttribute<TargeterType> TARGET = new EnumSpellAttribute<>("target",
            TargeterType.LINE_OF_SIGHT,
            AttributeDataType.TARGETER,
            TargeterType.class
    ).setSuggestions(TargeterType.values());

    SpellAttribute<Integer> MAX_TARGETS = new IntegerSpellAttribute("max_targets", 1)
            .setShowAttribute((val, attributable) -> val > 1 && attributable.getAttribute(TARGET) != TargeterType.SELF)
            // Multiply by fraction less than 1.5 -- it won't increase from 1 to 2, but it'll increase group targeting
            .typeModifier(SpellType.ARCANE, AttributeModifierType.MULTIPLY, AttributeDataType.DOUBLE, 1.4);

    SpellAttribute<Double> TARGET_RANGE = new DoubleSpellAttribute("target_range", 20)
            .setShowAttribute((val, attributable) -> attributable.getAttribute(TARGET) != TargeterType.SELF)
            .overrideTextureValue("range")
            .typeModifier(SpellType.ARCANE, AttributeModifierType.MULTIPLY, RangeAttributable.ARCANE_MULTIPLIER)
            .typeModifier(SpellType.ENDER, AttributeModifierType.MULTIPLY, RangeAttributable.ENDER_MULTIPLIER);

    SpellAttribute<Double> TARGET_RAY_SIZE = new DoubleSpellAttribute("target_ray_size", 1)
            .setShowAttribute((val, attributable) -> attributable.getAttribute(TARGET) == TargeterType.LINE_OF_SIGHT)
            .overrideTextureValue("range");

    Set<Class<? extends Entity>> ALLOWED_ARCHETYPES = Set.of(
            Mob.class,
            Creature.class,
            Monster.class,
            Illager.class,
            Raider.class,
            Animals.class,
            Fish.class,
            Golem.class
    );

    /**
     * Map of a tag key to the least specific entity class that it will override.<br/>
     * For example, UNDEAD -> Monster means if all entities are both in the UNDEAD tag
     * and the lowest common type is more specific than Monster, use that specific type.<br/>
     * If multiple tags match, the tag with the fewest elements is chosen.
     */
    Map<TagKey<EntityType>, Class<? extends Entity>> PREFERRED_ENTITY_TAGS = Map.of(
            EntityTypeTagKeys.UNDEAD, Monster.class,
            EntityTypeTagKeys.ARTHROPOD, Monster.class,
            EntityTypeTagKeys.SKELETONS, Monster.class,
            EntityTypeTagKeys.ZOMBIES, Monster.class
    );

    @AttributableSetupHandler
    default void setupTargeted() {
        addAttribute(MAX_TARGETS);
        addAttribute(TARGET);
        addAttribute(TARGET_RANGE);
    }

    Class<T> getEntityClass();

    default List<T> getTargets(CastContext context) {
        List<T> result;
        SpellInstance instance = context.instance();
        Player player = context.player();
        Location location = context.location();

        TargeterType targeterType = instance.getAttribute(TARGET);

        Class<T> entityClass = getEntityClass();

        switch (targeterType) {
            case SELF -> {
                if (entityClass.isInstance(player)) {
                    result = List.of(entityClass.cast(player));
                } else {
                    result = List.of();
                }
            }
            case LINE_OF_SIGHT -> {
                LineOfSightSelector<T> selector = new LineOfSightSelector<>(entityClass)
                        .setRange(instance.getAttribute(TARGET_RANGE))
                        .setRaySize(instance.getAttribute(TARGET_RAY_SIZE))
                        .setMaxSelections(instance.getAttribute(MAX_TARGETS))
                        .setPredicate(this::isValid)
                        .setDirection(location.getDirection());

                if (entityClass.isInstance(player)) {
                    selector.exclude(entityClass.cast(player));
                }

                result = selector
                        .select(location);
            }
            case RADIUS -> {
                RadiusSelector<T> selector = new RadiusSelector<>(entityClass)
                        .setRange(instance.getAttribute(TARGET_RANGE))
                        .setPredicate(this::isValid)
                        .setMaxSelections(instance.getAttribute(MAX_TARGETS));

                if (entityClass.isInstance(player)) {
                    selector.exclude(entityClass.cast(player));
                }

                result = selector.select(context.location());
            }
            default -> throw new IllegalStateException("Targeter missing: " + this);
        }

        return new LinkedList<>(result);
    }

    default List<T> applyToTargets(CastContext context, Consumer<T> consumer) {
        return tryApplyToTargets(context, target -> {
            consumer.accept(target);
            return true;
        });
    }
    default List<T> tryApplyToTargets(CastContext context, Function<T, Boolean> function) {
        List<T> targets = getTargets(context);

        List<T> affectedTargets = new LinkedList<>();
        for (T target : targets) {
            boolean affected = function.apply(target);
            if (affected) {
                affectedTargets.add(target);
                runTrigger(context, target);
            }
        }

        return affectedTargets;
    }

    default void runTrigger(CastContext context, T target) {
        TargeterType type = context.instance().getAttribute(TARGET);

        SpellTriggeredEvent<Entity> trigger = SpellTriggeredEvents.DIRECT_TARGET_ENTITY_TRIGGER;
        if (type == TargeterType.SELF) {
            trigger = SpellTriggeredEvents.TARGET_SELF_TRIGGER;
        }

        context.runEffects(trigger, target);
    }

    default boolean isValid(T entity) {
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity.hasAI();
        }
        return true;
    }

    default String getNoTargetsMessage(CastContext context) {
        TargeterType targeterType = context.instance().getAttribute(TARGET);

        return switch (targeterType) {
            case SELF -> "Cannot target yourself!";
            case LINE_OF_SIGHT -> "No valid targets in line of sight!";
            case RADIUS -> "No valid targets in radius!";
        };
    }

    default Component groupName(List<T> targets) {
        if (targets.size() == 1) {
            return targets.getFirst().name();
        } else {
            Class<T> entityClass = getEntityClass();
            Set<Class<? extends T>> superTypes = getAllSupertypes(targets.getFirst().getClass(), entityClass);
            for (T target : targets) {
                superTypes.retainAll(getAllSupertypes(target.getClass(), entityClass));
            }

            superTypes.removeIf(cls -> cls.getCanonicalName().contains("craftbukkit"));
            // Remove all classes that aren't either in the whitelist, or a specific entity type class.
            superTypes.removeIf(cls ->
                    !ALLOWED_ARCHETYPES.contains(cls) &&
                            Arrays.stream(EntityType.values()).map(EntityType::getEntityClass).noneMatch(cls::equals)
            );

            List<Class<? extends T>> lowestCommonTypes = superTypes.stream()
                    // If all other classes are assignable from `check`, then check extends them all,
                    // which means it's (among) the lowest.
                    .filter(check -> superTypes.stream()
                            .allMatch(cls -> cls.isAssignableFrom(check)))
                    .toList();

            Component typeComponent;
            if (lowestCommonTypes.isEmpty()) {
                typeComponent = Component.text(getEntityClass().getName());
            } else {
                EntityType lowestType = Arrays.stream(EntityType.values())
                        .filter(type -> lowestCommonTypes.contains(type.getEntityClass()))
                        .findAny()
                        .orElse(null);

                if (lowestType != null) {
                    typeComponent = Component.translatable(lowestType.translationKey());
                } else {
                    Class<? extends T> lowestCommonType = lowestCommonTypes.getFirst();

                    Set<TagKey<EntityType>> matchedKeys = new HashSet<>();
                    for (TagKey<EntityType> tagKey : PREFERRED_ENTITY_TAGS.keySet()) {
                        Class<? extends Entity> mostSpecificToOverride = PREFERRED_ENTITY_TAGS.get(tagKey);

                        if (lowestCommonType != mostSpecificToOverride && mostSpecificToOverride.isAssignableFrom(lowestCommonType)) {
                            // The lowest common type found is more specific than mostSpecificToOverride
                            continue;
                        }

                        boolean matchingTag = targets.stream()
                                .map(Entity::getType)
                                .allMatch(type -> WbsRegistryUtil.isTagged(type, tagKey));

                        if (matchingTag) {
                            matchedKeys.add(tagKey);
                        }
                    }

                    TagKey<EntityType> matchedTag = null;
                    if (!matchedKeys.isEmpty()) {
                        Registry<EntityType> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENTITY_TYPE);
                        matchedTag = matchedKeys.stream()
                                .min(Comparator.comparing(tagKey -> registry.getTagValues(tagKey).size()))
                                .orElse(null);
                    }

                    if (matchedTag != null) {
                        typeComponent = Component.text(
                                WbsStrings.capitalizeAll(matchedTag.key().value().replace("_", " "))
                        );
                    } else {
                        typeComponent = Component.text(lowestCommonType.getSimpleName());
                    }
                }
            }

            return typeComponent.append(Component.text(" x" + targets.size()));
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Set<Class<? extends T>> getAllSupertypes(Class<?> clazz, Class<T> extending) {
        Set<Class<? extends T>> supertypes = new HashSet<>();

        if (!extending.isAssignableFrom(clazz)) {
            return supertypes;
        }

        supertypes.add((Class<? extends T>) clazz);

        if (!clazz.isInterface() && clazz != Object.class) {
            supertypes.addAll(getAllSupertypes(clazz.getSuperclass(), extending));
        }

        for (Class<?> implementedInterface : clazz.getInterfaces()) {
            supertypes.addAll(getAllSupertypes(implementedInterface, extending));
        }

        return supertypes;
    }

    enum TargeterType {
        SELF,
        LINE_OF_SIGHT,
        RADIUS,
    }
}
