package wbs.wandcraft.objects.colliders;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

public class Collision {

    @NotNull
    private final MagicObjectCollider collider;
    @NotNull
    private final Location hitLocation;
    @NotNull
    private Vector normal;

    public Collision(@NotNull MagicObjectCollider collider, @NotNull Location hitLocation, @NotNull Vector normal) {
        this.collider = collider;

        this.hitLocation = hitLocation;
        this.normal = normal.normalize();
    }

    @NotNull
    public MagicObjectCollider getCollider() {
        return collider;
    }

    @NotNull
    public Location getHitLocation() {
        return hitLocation.clone();
    }

    @NotNull
    public Vector getNormal() {
        return normal.clone();
    }

    public void setNormal(@NotNull Vector normal) {
        this.normal = normal;
    }
}
