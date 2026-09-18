package wbs.wandcraft.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.attributes.SpellAttribute;

public interface DistanceScaler {
    double MIN_SCALING_DISTANCE = 0.05;

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
        if (distSquared < DistanceScaler.MIN_SCALING_DISTANCE) {
            distSquared = DistanceScaler.MIN_SCALING_DISTANCE;
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

}
