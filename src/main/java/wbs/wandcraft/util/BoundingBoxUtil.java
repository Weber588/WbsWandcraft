package wbs.wandcraft.util;

import org.bukkit.Location;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

public class BoundingBoxUtil {
    public static Location getClosestInBounds(BoundingBox box, Location target) {
        return getClosestInBounds(box, target.toVector()).toLocation(target.getWorld());
    }
    public static Vector getClosestInBounds(BoundingBox box, Vector target) {
        return new Vector(
                Math.clamp(target.getX(), box.getMinX(), box.getMaxX()),
                Math.clamp(target.getY(), box.getMinY(), box.getMaxY()),
                Math.clamp(target.getZ(), box.getMinZ(), box.getMaxZ())
        );
    }
}
