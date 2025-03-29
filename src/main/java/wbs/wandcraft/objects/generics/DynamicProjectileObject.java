package wbs.wandcraft.objects.generics;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsSoundGroup;
import wbs.wandcraft.spell.definitions.extensions.CastContext;

import java.util.function.Predicate;

public class DynamicProjectileObject extends DynamicMagicObject {

    protected double range = 100;
    @NotNull
    protected WbsSoundGroup hitSound = new WbsSoundGroup();

    @NotNull
    private Runnable maxDistanceReached = () -> {};

    public DynamicProjectileObject(Location location, Player caster, CastContext context) {
        super(location, caster, context);

        setEntityPredicate(Predicate.not(caster::equals));
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
    protected boolean onStep(int step, int stepsThisTick) {
        debug("Projectile object onStep()");
        boolean cancel = super.onStep(step, stepsThisTick);

        setStepsPerTick(getVelocity().length() * 5);

        if (getAge() * getStepsPerTick() > 5 && effects != null) {
            effects.buildAndPlay(location);
        }

        if (getLocation().distanceSquared(getSpawnLocation()) > range * range) {
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
}
