package wbs.wandcraft.context;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// Manage casting states of players transiently, to avoid desync between server and persistent data on crashes/issues
@NullMarked
public class CastingManager {
    private static final Map<UUID, CastingQueue> CASTING_PLAYERS = new HashMap<>();

    public static void setCasting(Player player, CastingQueue castingQueue) {
        setCasting(player.getUniqueId(), castingQueue);
    }
    public static void setCasting(UUID uuid, CastingQueue castingQueue) {
        if (CASTING_PLAYERS.containsKey(uuid)) {
            throw new IllegalStateException(uuid + " is already casting!");
        }

        CASTING_PLAYERS.put(uuid, castingQueue);
    }

    @Nullable
    public static CastingQueue getCurrentContext(Player player) {
        return getCurrentContext(player.getUniqueId());
    }

    @Nullable
    public static CastingQueue getCurrentContext(UUID uuid) {
        return CASTING_PLAYERS.get(uuid);
    }

    public static boolean isCasting(Player player) {
        return isCasting(player.getUniqueId());
    }
    public static boolean isCasting(UUID uuid) {
        return CASTING_PLAYERS.containsKey(uuid);
    }

    public static void stopCasting(Player player) {
        stopCasting(player.getUniqueId());
    }
    public static void stopCasting(UUID uuid) {
        CASTING_PLAYERS.remove(uuid);
    }
}
