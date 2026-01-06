package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import wbs.utils.util.entities.selector.LineOfSightSelector;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.wandcraft.RegisteredPersistentDataType;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.EnumSpellAttribute;
import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;

import java.util.List;

public interface TargetedSpell<T extends Entity> extends ISpellDefinition {
    SpellAttribute<TargeterType> TARGET = new EnumSpellAttribute<>("target",
            TargeterType.LINE_OF_SIGHT,
            RegisteredPersistentDataType.TARGETER,
            TargeterType.class
    );
    SpellAttribute<Integer> MAX_TARGETS = new IntegerSpellAttribute("max_targets", 1)
            .setShowAttribute((value, attributable) -> value > 1 && attributable.getAttribute(TARGET) != TargeterType.SELF);
    SpellAttribute<Double> TARGET_RANGE = new DoubleSpellAttribute("target_range", 20)
            .setShowAttribute((value, attributable) -> attributable.getAttribute(TARGET) != TargeterType.SELF)
            .overrideTextureValue("range");

    default void setupTargeted() {
        addAttribute(MAX_TARGETS);
        addAttribute(TARGET);
    }

    Class<T> getEntityClass();

    default List<T> getTargets(CastContext context) {
        SpellInstance instance = context.instance();
        Player player = context.player();
        Location location = context.location();

        TargeterType targeterType = instance.getAttribute(TARGET);

        Class<T> entityClass = getEntityClass();

        switch (targeterType) {
            case SELF -> {
                if (entityClass.isInstance(player)) {
                    return List.of(entityClass.cast(player));
                } else {
                    return List.of();
                }
            }
            case LINE_OF_SIGHT -> {
                LineOfSightSelector<T> selector = new LineOfSightSelector<>(entityClass)
                        .setRange(instance.getAttribute(TARGET_RANGE))
                        .setMaxSelections(instance.getAttribute(MAX_TARGETS))
                        .setDirection(location.getDirection());

                if (entityClass.isInstance(player)) {
                    selector.exclude(entityClass.cast(player));
                }

                return selector
                        .select(location);
            }
            case RADIUS -> {
                RadiusSelector<T> selector = new RadiusSelector<>(entityClass)
                        .setRange(instance.getAttribute(TARGET_RANGE))
                        .setMaxSelections(instance.getAttribute(MAX_TARGETS));

                if (entityClass.isInstance(player)) {
                    selector.exclude(entityClass.cast(player));
                }

                return selector.select(context.location());
            }
            default -> throw new IllegalStateException("Targeter missing: " + this);
        }
    }

    enum TargeterType {
        SELF,
        LINE_OF_SIGHT,
        RADIUS,
    }
}
