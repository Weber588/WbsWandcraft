package wbs.wandcraft.spell.trigger;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.RayTraceResult;
import wbs.wandcraft.objects.generics.MagicObject;

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
    public static final SupportedEvent<Block, RayTraceResult> BLOCK_RAYTRACE =
            new SupportedEvent<>(RayTraceResult.class, RayTraceResult::getHitBlock);
    public static final SupportedEvent<Location, MagicObject> LOCATION_MAGIC_OBJECT =
            new SupportedEvent<>(MagicObject.class, MagicObject::getLocation);
    public static <T extends Entity> SupportedEvent<T, RayTraceResult> entityFromRaytraceEvent(Class<T> clazz) {
        return new SupportedEvent<>(RayTraceResult.class, (RayTraceResult rayTraceResult) -> {
            Entity hitEntity = rayTraceResult.getHitEntity();
            if (clazz.isInstance(hitEntity)) {
                return clazz.cast(hitEntity);
            }
            return null;
        });
    }

    private final Class<O> eventClass;
    private final Function<O, T> function;

    public SupportedEvent(Class<O> eventClass, Function<O, T> function) {
        this.eventClass = eventClass;
        this.function = function;
    }

    public Class<O> eventClass() {
        return eventClass;
    }

    public Function<O, T> function() {
        return function;
    }
}
