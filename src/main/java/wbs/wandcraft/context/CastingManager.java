package wbs.wandcraft.context;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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

    public static void startCasting(Entity entity, CastingQueue castingQueue) {
        startCasting(entity.getUniqueId(), castingQueue);
    }
    public static void startCasting(UUID uuid, CastingQueue castingQueue) {
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

    public static boolean stopCasting(Entity entity) {
        return stopCasting(entity.getUniqueId());
    }
    public static boolean stopCasting(UUID uuid) {
        return CASTING.remove(uuid) != null;
    }

    public static void startConcentrating(Entity entity, CastContext context) {
        startConcentrating(entity.getUniqueId(), context);
    }
    public static void startConcentrating(UUID uuid, CastContext context) {
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

    public static boolean isConcentrating(Entity entity, CastContext context) {
        return isConcentrating(entity.getUniqueId(), context);
    }
    public static boolean isConcentrating(UUID uuid, CastContext context) {
        return context.equals(CONCENTRATING.get(uuid));
    }

    public static boolean stopConcentrating(Entity entity) {
        return stopConcentrating(entity.getUniqueId());
    }
    public static boolean stopConcentrating(UUID uuid) {
        return CONCENTRATING.remove(uuid) != null;
    }

    public static boolean stopConcentrating(Entity entity, CastContext context) {
        return stopConcentrating(entity.getUniqueId(), context);
    }
    public static boolean stopConcentrating(UUID uuid, CastContext context) {
        if (isConcentrating(uuid, context)) {
            CONCENTRATING.remove(uuid);
            return true;
        }
        return false;
    }

    public static boolean interruptConcentration(LivingEntity entity) {
        boolean stopped = stopConcentrating(entity.getUniqueId());
        if (stopped) {
            Component concentrationMessage = Component.text("Concentration broken!").color(NamedTextColor.RED);
            entity.sendActionBar(concentrationMessage);
        }
        return stopped;
    }
}
