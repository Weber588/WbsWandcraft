package wbs.wandcraft.objects.generics;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsMath;
import wbs.wandcraft.events.objects.DynamicObjectBounceEvent;
import wbs.wandcraft.events.objects.DynamicObjectPhysicsEvent;
import wbs.wandcraft.events.objects.MagicObjectMoveEvent;
import wbs.wandcraft.objects.colliders.Collider;
import wbs.wandcraft.spell.definitions.extensions.CastContext;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class DynamicMagicObject extends KinematicMagicObject {

    // Distance per tick
    @NotNull
    private Vector velocity = new Vector();
    // Acceleration per tick
    @NotNull
    private Vector acceleration = new Vector();

    @NotNull
    private final Vector gravity = new Vector(0, 0, 0);

    private boolean doCollisions = true;
    @NotNull
    private FluidCollisionMode fluidCollisionMode = FluidCollisionMode.NEVER;

    private boolean doBounces = false;
    private int maxBounces = Integer.MAX_VALUE;
    private int currentBounces = 0;

    private boolean hitEntities = true;
    private Predicate<Entity> entityPredicate = entity -> true;
    private double hitBoxSize = 0;

    // ================================== //
    //           Step Management          //
    // ================================== //

    private int stepsTaken = 0;

    // How many steps per tick on average
    private double stepsPerTick = 1;

    // How much error has been introduced so far by using int instead of double
    private double error = 0;
    // How much error increases by every step
    private double errorPerStep;

    @NotNull
    private Function<RayTraceResult, Boolean> onHitBlock = (result) -> false;
    private Function<RayTraceResult, Boolean> onHitEntity = (result) -> false;

    public DynamicMagicObject(Location location, Player caster, CastContext context) {
        super(location, caster, context);
    }

    @Override
    protected void onRun() {
        super.onRun();
    }

    @Override
    protected final boolean tick() {
        debug("Ticking dynamic magic object");
        error += errorPerStep;

        // How many steps run every tick before considering error
        int baseStepsPerTick = (int) stepsPerTick;
        errorPerStep = stepsPerTick - baseStepsPerTick;

        int stepsThisTick = baseStepsPerTick;
        if (error >= 1) {
            stepsThisTick+= 1;
            error -= 1;
        }

        boolean cancel;
        Vector accelerationPerStep = perStep(acceleration.clone());
        for (int step = 0; step < stepsThisTick; step++) {
            if (isExpired()) return true;
            stepsTaken++;

            applyPhysics(accelerationPerStep);

            cancel = move();
            if (cancel) {
                debug("Cancelled in move()");
                return true;
            }

            cancel = onStep(step, stepsThisTick);
            if (cancel) {
                debug("Cancelled in onStep");
                return true;
            }
        }
        acceleration.multiply(0);

        return false;
    }

    /**
     * Runs each physics step.
     * @param step Which step this is on the current tick.
     * @param stepsThisTick How many steps will run in the current tick
     * @return Whether or not to cancel. True to prevent future steps,
     * and expire the magic object
     */
    protected boolean onStep(int step, int stepsThisTick) {
        return false;
    }

    // ================================== //
    //          Movement/Physics          //
    // ================================== //

    /**
     * Called stepsPerTick times per tick.
     */
    protected boolean move() {
        debug("Dynamic object move()");
        boolean cancel = beforeMove();

        Vector velocityThisStep = perStep(velocity);
        Location newLocation;

        if (doCollisions) {
            RayTraceResult result;

            if (!hitEntities) {
                result = world.rayTraceBlocks(
                                getLocation(),
                                velocity,
                                velocityThisStep.length(),
                                fluidCollisionMode,
                                true
                        );
            } else {
                result = world.rayTrace(
                        getLocation(),
                        velocity,
                        velocityThisStep.length(),
                        fluidCollisionMode,
                        true,
                        hitBoxSize,
                        entityPredicate
                );
            }

            if (result == null) {
                newLocation = getLocation().add(velocityThisStep);
            } else {
                Location hitLocation = result.getHitPosition().toLocation(world);
                if (result.getHitBlock() != null) {
                    BlockFace face = Objects.requireNonNull(result.getHitBlockFace());

                    if (doBounces) {
                        debug("DynamicMagicObject hit block. Trying bounce... Result = (" + result + ")");
                        if (currentBounces < maxBounces) {
                            currentBounces++;

                            DynamicObjectBounceEvent event = new DynamicObjectBounceEvent(this, hitLocation, face.getDirection());

                            Bukkit.getPluginManager().callEvent(event);

                            if (event.isCancelled()) {
                                newLocation = getLocation().add(velocityThisStep);
                                cancel |= onHitBlock.apply(result);
                                debug("DynamicObjectBounceEvent cancelled. Cancelled = " + cancel);
                            } else {
                                velocity = WbsMath.reflectVector(velocity, face.getDirection());
                                velocityThisStep = WbsMath.reflectVector(velocityThisStep, face.getDirection());

                                double distanceToBounce = hitLocation.distance(getLocation());
                                double distanceLeft = velocityThisStep.length() - distanceToBounce;

                                velocityThisStep = WbsMath.scaleVector(velocityThisStep, distanceLeft);

                                newLocation = getLocation().add(velocityThisStep);

                                onBounce();
                            }
                        } else {
                            newLocation = getLocation().add(velocityThisStep);
                            cancel |= onHitBlock.apply(result);
                        }
                    } else {
                        newLocation = getLocation().add(velocityThisStep);
                        cancel |= onHitBlock.apply(result);
                        debug("DynamicMagicObject hit block. Result = (" + result + "). Cancel = " + cancel);
                    }
                } else {
                    newLocation = getLocation().add(velocityThisStep);
                }

                if (result.getHitEntity() != null) {
                    cancel |= onHitEntity.apply(result);
                    debug("DynamicMagicObject hit entity. Result = (" + result + "). Cancel = " + cancel);
                }
            }
        } else {
            newLocation = getLocation().add(velocityThisStep);
        }

        MagicObjectMoveEvent event = new MagicObjectMoveEvent(this, newLocation);

        for (MagicObject object : Collider.getObjectsWithColliders()) {
            if (object == this) continue;
            object.collider.tryColliding(event);
            if (event.isCancelled()) return cancel;
        }

        if (event.isCancelled()) return cancel;

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return cancel;

        Location finalNewLocation = event.getNewLocation();
        Location currentLocation = getLocation();

        setLocation(finalNewLocation);

        cancel |= afterMove(currentLocation, finalNewLocation);

        debug("Dynamic object move() finished -- cancel = " + cancel);
        return cancel;
    }

    /**
     * Make this object bounce off a plane defined by
     * the provided normal
     * @param normal The normal representing the plane to bounce off
     * @return Whether or not the bounce was successful.
     */
    public boolean bounce(Vector normal) {
        DynamicObjectBounceEvent event = new DynamicObjectBounceEvent(this, getLocation(), normal);

        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            setDirection(WbsMath.reflectVector(velocity, normal));

            onBounce();
        }

        return !event.isCancelled();
    }

    /**
     * Called stepsPerTick times per tick.
     * Calculates velocity and decreases acceleration.
     */
    private void applyPhysics(Vector accelerationThisStep) {
        DynamicObjectPhysicsEvent event = new DynamicObjectPhysicsEvent(this, accelerationThisStep);

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        accelerationThisStep = event.getAccelerationThisStep();

        velocity.add(accelerationThisStep);
        velocity.add(perStep(gravity));
    }

    /**
     * Called before moving each step
     * @return True to make the object expire
     */
    protected boolean beforeMove() {
        return false;
    }

    /**
     * Called after moving each step
     * @return True to make the object expire
     */
    protected boolean afterMove(Location oldLocation, Location newLocation) {
        return false;
    }

    protected void onBounce() {

    }

    // ================================== //
    //           Utility Methods          //
    // ================================== //

    public void applyForce(Vector acceleration) {
        this.acceleration.add(acceleration);
    }

    protected Vector perStep(Vector perTicks) {
        return perTicks.clone().multiply(1 / stepsPerTick);
    }

    // ================================== //
    //           Getters/Setters          //
    // ================================== //

    public DynamicMagicObject setOnHitBlock(@NotNull Function<RayTraceResult, Boolean> onHitBlock) {
        this.onHitBlock = onHitBlock;
        return this;
    }

    public DynamicMagicObject setOnHitEntity(@NotNull Function<RayTraceResult, Boolean> onHitEntity) {
        this.onHitEntity = onHitEntity;
        return this;
    }

    public DynamicMagicObject setOnHit(@NotNull Function<RayTraceResult, Boolean> onHit) {
        this.onHitEntity = onHit;
        this.onHitBlock = onHit;
        return this;
    }

    public Vector getVelocity() {
        return velocity.clone();
    }

    public DynamicMagicObject setVelocity(Vector velocityInTicks) {
        velocity = velocityInTicks;
        return this;
    }
    public DynamicMagicObject setVelocityInSeconds(Vector velocityInSeconds) {
        velocity = velocityInSeconds.clone().multiply(0.05);
        return this;
    }

    public DynamicMagicObject setSpeed(double speedInTicks) {
        velocity = scaleVector(velocity, speedInTicks);
        return this;
    }

    public DynamicMagicObject setSpeedInSeconds(double speedInSeconds) {
        velocity = scaleVector(velocity, speedInSeconds / 20);
        return this;
    }

    public DynamicMagicObject setDirection(Vector direction) {
        velocity = WbsMath.scaleVector(direction.clone(), velocity.length());
        return this;
    }

    public Vector getAcceleration() {
        return acceleration.clone();
    }

    public DynamicMagicObject setAcceleration(Vector acceleration) {
        this.acceleration = acceleration;
        return this;
    }

    public double getStepsPerTick() {
        return stepsPerTick;
    }

    /**
     * Set how many times this object calculates steps
     * in a tick (on average).
     * @param stepsPerTick The number of times to calculate
     *                     physics per tick on average.
     */
    public DynamicMagicObject setStepsPerTick(double stepsPerTick) {
        this.stepsPerTick = stepsPerTick;
        return this;
    }

    public DynamicMagicObject setStepsPerSecond(double stepsPerSecond) {
        this.stepsPerTick = stepsPerSecond / 20;
        return this;
    }

    public double getGravity() {
        return gravity.getY();
    }

    public DynamicMagicObject setGravity(double gravityPerTick) {
        gravity.setY(-gravityPerTick);
        return this;
    }

    public DynamicMagicObject setGravityInSeconds(double gravityPerSecond) {
        gravity.setY(-gravityPerSecond / 20);
        return this;
    }

    public boolean doCollisions() {
        return doCollisions;
    }

    public DynamicMagicObject setDoCollisions(boolean doCollisions) {
        this.doCollisions = doCollisions;
        return this;
    }

    @NotNull
    public FluidCollisionMode getFluidCollisionMode() {
        return fluidCollisionMode;
    }

    public DynamicMagicObject setFluidCollisionMode(@NotNull FluidCollisionMode fluidCollisionMode) {
        this.fluidCollisionMode = fluidCollisionMode;
        return this;
    }

    public boolean doBounces() {
        return doBounces;
    }

    public DynamicMagicObject setDoBounces(boolean doBounces) {
        this.doBounces = doBounces;
        return this;
    }

    public int getMaxBounces() {
        return maxBounces;
    }

    public DynamicMagicObject setMaxBounces(int maxBounces) {
        this.maxBounces = maxBounces;
        return this;
    }

    public boolean isDoBounces() {
        return doBounces;
    }

    public boolean isHitEntities() {
        return hitEntities;
    }

    public DynamicMagicObject setHitEntities(boolean hitEntities) {
        this.hitEntities = hitEntities;
        return this;
    }

    public Predicate<Entity> getEntityPredicate() {
        return entityPredicate;
    }

    public DynamicMagicObject setEntityPredicate(Predicate<Entity> entityPredicate) {
        this.entityPredicate = entityPredicate;
        return this;
    }

    public double getHitBoxSize() {
        return hitBoxSize;
    }

    public DynamicMagicObject setHitBoxSize(double hitBoxSize) {
        this.hitBoxSize = hitBoxSize;
        return this;
    }

    public int stepsTaken() {
        return stepsTaken;
    }

    @Override
    public String toString() {
        return super.toString() +
                ", velocity=" + velocity +
                ", acceleration=" + acceleration +
                ", gravity=" + gravity +
                ", doCollisions=" + doCollisions +
                ", fluidCollisionMode=" + fluidCollisionMode +
                ", doBounces=" + doBounces +
                ", maxBounces=" + maxBounces +
                ", currentBounces=" + currentBounces +
                ", hitEntities=" + hitEntities +
                ", entityPredicate=" + entityPredicate +
                ", hitBoxSize=" + hitBoxSize +
                ", stepsTaken=" + stepsTaken +
                ", stepsPerTick=" + stepsPerTick +
                ", error=" + error +
                ", errorPerStep=" + errorPerStep +
                ", onHitBlock=" + onHitBlock +
                ", onHitEntity=" + onHitEntity
                ;
    }
}
