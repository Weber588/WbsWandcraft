package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.string.WbsStrings;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.attributes.Attributable;
import wbs.wandcraft.spell.attributes.SpellAttribute;

import java.util.Collection;

@NullMarked
public interface ISpellDefinition extends Keyed, Attributable {
    double MIN_SCALING_DISTANCE = 0.05;
    void addAttribute(SpellAttribute<?> attribute);

    Collection<SpellAttribute<?>> getAttributes();

    default String name() {
        return WbsStrings.capitalizeAll(key().value().replace("_", " "));
    }

    Component displayName();

    default <T extends Number> T scaleByDistance(CastContext context, Entity target, double varianceClamp, SpellAttribute<T> attribute) {
        T value = context.instance().getAttribute(attribute);
        return scaleByDistance(context, target, varianceClamp, value);
    }
    @SuppressWarnings("unchecked")
    default <T extends Number> T scaleByDistance(CastContext context, Entity target, double varianceClamp, T value) {
        Location entityLocation = target instanceof LivingEntity living ? living.getEyeLocation() : target.getLocation();
        Vector centerToTarget = entityLocation // Give a slight upwards force by using eye height
                .subtract(context.location())
                .toVector();

        double distSquared = centerToTarget.lengthSquared();
        if (distSquared < MIN_SCALING_DISTANCE) {
            distSquared = MIN_SCALING_DISTANCE;
        }

        double scaled = Math.clamp(value.doubleValue() / distSquared, value.doubleValue() / varianceClamp, value.doubleValue() * varianceClamp);

        return switch (value) {
            case Double _ -> (T) Double.valueOf(scaled);
            case Integer _ -> (T) Integer.valueOf((int) scaled);
            case Long _ -> (T) Long.valueOf((long) scaled);
            case Float _ -> (T) Float.valueOf(((float) scaled));
            default -> throw new UnsupportedOperationException("Scaling not implemented for " + value.getClass().getCanonicalName());
        };
    }

    default void debug(String message) {
        String channel = "spell_%s";

        String id;
        if (key().namespace().equals(WbsWandcraft.getInstance().namespace())) {
            id = key().value();
        } else {
            id = key().asString();
        }

        WbsWandcraft.getInstance().debug(channel.formatted(id), message);
    }
}
