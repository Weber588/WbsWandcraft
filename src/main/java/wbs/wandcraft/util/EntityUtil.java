package wbs.wandcraft.util;

import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Optional;

public class EntityUtil {
    public static final int MAX_TP_ATTEMPTS = 10;

    public static boolean tryRandomTeleport(LivingEntity entity, double range) {
        World world = entity.getWorld();
        boolean teleported = false;

        int attempts = 0;
        while (!teleported && ++attempts < MAX_TP_ATTEMPTS) {
            double x = entity.getX() + range * ((Math.random() * 2) - 1);
            double y = Math.clamp(entity.getY() + range * ((Math.random() * 2) - 1), world.getMinHeight(), world.getLogicalHeight());
            double z = entity.getZ() + range * ((Math.random() * 2) - 1);

            Optional<Boolean> teleportAttempt = ((CraftLivingEntity) entity).getHandle().randomTeleport(x, y, z, true, PlayerTeleportEvent.TeleportCause.PLUGIN);

            teleported = teleportAttempt.orElse(false);
        }
        return teleported;
    }
}
