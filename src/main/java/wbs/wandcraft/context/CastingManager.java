package wbs.wandcraft.context;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Manage casting states of players transiently, to avoid desync between server and persistent data on crashes/issues
@NullMarked
public class CastingManager {
    private static final Map<UUID, CastingQueue> CASTING = new HashMap<>();
    private static final Map<UUID, CastContext> CONCENTRATING = new HashMap<>();

    public static void setCasting(Entity entity, CastingQueue castingQueue) {
        setCasting(entity.getUniqueId(), castingQueue);
    }
    public static void setCasting(UUID uuid, CastingQueue castingQueue) {
        if (CASTING.containsKey(uuid)) {
            throw new IllegalStateException(uuid + " is already casting!");
        }

        CASTING.put(uuid, castingQueue);
    }

    @Nullable
    public static CastingQueue getCurrentQueue(Entity entity) {
        return getCurrentQueue(entity.getUniqueId());
    }

    @Nullable
    public static CastingQueue getCurrentQueue(UUID uuid) {
        return CASTING.get(uuid);
    }

    public static boolean isCasting(Entity entity) {
        return isCasting(entity.getUniqueId());
    }
    public static boolean isCasting(UUID uuid) {
        return CASTING.containsKey(uuid);
    }

    public static void stopCasting(Entity entity) {
        stopCasting(entity.getUniqueId());
    }
    public static void stopCasting(UUID uuid) {
        CASTING.remove(uuid);
    }

    public static void setConcentrating(Entity entity, CastContext context) {
        setConcentrating(entity.getUniqueId(), context);
    }
    public static void setConcentrating(UUID uuid, CastContext context) {
        CONCENTRATING.put(uuid, context);
    }

    @Nullable
    public static CastContext getConcentratingOn(Entity entity) {
        return getConcentratingOn(entity.getUniqueId());
    }

    @Nullable
    public static CastContext getConcentratingOn(UUID uuid) {
        return CONCENTRATING.get(uuid);
    }

    public static boolean isConcentrating(Entity entity) {
        return isConcentrating(entity.getUniqueId());
    }
    public static boolean isConcentrating(UUID uuid) {
        return CONCENTRATING.containsKey(uuid);
    }

    public static boolean isConcentratingOn(Entity entity, CastContext context) {
        return isConcentratingOn(entity.getUniqueId(), context);
    }
    public static boolean isConcentratingOn(UUID uuid, CastContext context) {
        return context.equals(CONCENTRATING.get(uuid));
    }

    public static void stopConcentrating(Entity entity) {
        stopConcentrating(entity.getUniqueId());
    }
    public static void stopConcentrating(UUID uuid) {
        CONCENTRATING.remove(uuid);
    }
}
