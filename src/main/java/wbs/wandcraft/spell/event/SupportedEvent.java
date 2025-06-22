package wbs.wandcraft.spell.event;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.RayTraceResult;

import java.util.Objects;
import java.util.function.Function;

public final class SupportedEvent<T, O> {
    public static final SupportedEvent<Location, RayTraceResult> LOCATION_RAYTRACE =
            new SupportedEvent<>(RayTraceResult.class, result -> {
                World world = null;

                Entity hitEntity = result.getHitEntity();
                if (hitEntity != null) {
                    world = hitEntity.getWorld();
                }

                Block hitBlock = result.getHitBlock();
                if (hitBlock != null) {
                    world = hitBlock.getWorld();
                }

                return result.getHitPosition().toLocation(Objects.requireNonNull(world));
            });

    private final Class<O> eventClass;
    private final Function<O, T> function;

    public SupportedEvent(Class<O> eventClass, Function<O, T> function) {
        this.eventClass = eventClass;
        this.function = function;
    }

    public Class<O> eventClass() {
        return eventClass;
    }

    public T toEventClass(O other) {
        return function.apply(other);
    }


    public T transform(O other) {
        return function.apply(other);
    }

    public Function<O, T> function() {
        return function;
    }
}
