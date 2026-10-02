package wbs.wandcraft.objects.generics;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsSoundGroup;
import wbs.wandcraft.context.CastContext;

public class DynamicProjectileObject extends DynamicMagicObject {

    protected double range = 100;
    @NotNull
    protected WbsSoundGroup hitSound = new WbsSoundGroup();

    @NotNull
    private Runnable maxDistanceReached = () -> {};

    private boolean shouldPlayParticles = false;

    public DynamicProjectileObject(Location location, Player caster, CastContext context) {
        super(location, caster, context);

        setEntityPredicate(entity -> {
            if (entity.equals(caster)) {
                return false;
            }

            Entity follower = follower();
            if (entity.equals(follower)) {
                return false;
            }

            return true;
        });
        setOnHitBlock((result) -> true);
        setOnHitEntity((result) -> true);
    }

    public @NotNull WbsSoundGroup getHitSound() {
        return hitSound;
    }

    public void setHitSound(@NotNull WbsSoundGroup hitSound) {
        this.hitSound = hitSound;
    }

    @Override
    protected boolean beforeMove() {
        setStepsPerTick(getVelocity().length() * 5);
        return super.beforeMove();
    }

    @Override
    protected boolean onStep(int step, int stepsThisTick) {
        debug("Projectile object onStep()");
        boolean cancel = super.onStep(step, stepsThisTick);

        double distanceFromSpawn = getLocation().distanceSquared(getSpawnLocation());

        if (tickEffects != null && (shouldPlayParticles || distanceFromSpawn > 1)) {
            debug("Projectile object playing effects");
            shouldPlayParticles = true;
            tickEffects.buildAndPlay(location);
        }

        if (distanceFromSpawn > range * range) {
            debug("Projectile object left range in onStep -- cancelling (" + distanceFromSpawn + " > " + range * range);
            cancel = true;
            maxDistanceReached.run();
        }

        return cancel;
    }

    public double getRange() {
        return range;
    }

    public DynamicProjectileObject setRange(double range) {
        this.range = range;
        return this;
    }

    public DynamicProjectileObject setMaxDistanceReached(@NotNull Runnable maxDistanceReached) {
        this.maxDistanceReached = maxDistanceReached;
        return this;
    }

    @Override
    public String toString() {
        return super.toString() +
                ", range=" + range +
                ", hitSound=" + hitSound +
                ", maxDistanceReached=" + maxDistanceReached
                ;
    }
}
