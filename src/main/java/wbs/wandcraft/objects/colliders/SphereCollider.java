package wbs.wandcraft.objects.colliders;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.objects.generics.MagicObject;

public class SphereCollider extends Collider {

    private double radius;

    public SphereCollider(MagicObject parent, double radius) {
        super(parent);
        this.radius = radius;
    }

    @Override
    @Nullable
    public Collision getCollision(Location start, Location end) {
        double newDist = end.distance(getLocation());
        double currentDist = start.distance(getLocation());

        if ((collideOnEnter && newDist < radius && currentDist > radius) ||
                (collideOnLeave && newDist > radius && currentDist < radius))
        {
            boolean entering = newDist < radius;

            Vector normal = start.clone().subtract(getLocation()).toVector();

            Location hitPos = normal.normalize()
                    .multiply(radius)
                    .toLocation(getWorld())
                    .add(getLocation());

            Location offset = normal.normalize()
                    .multiply(radius * (1 + (entering ? -0.5 : 0.5)))
                    .toLocation(getWorld())
                    .add(getLocation());

            normal = start.clone().subtract(offset).toVector();

            return new Collision(this, hitPos, normal);
        }

        return null;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }
}
