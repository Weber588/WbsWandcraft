package wbs.wandcraft.objects.generics;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsSoundGroup;
import wbs.wandcraft.spell.definitions.SpellInstance;

public class DynamicProjectileObject extends DynamicMagicObject {

    protected double range = 100;
    @NotNull
    protected WbsSoundGroup hitSound = new WbsSoundGroup();

    @NotNull
    private Runnable maxDistanceReached = () -> {};

    public DynamicProjectileObject(Location location, Player caster, SpellInstance castingSpell) {
        super(location, caster, castingSpell);

        setEntityPredicate(caster::equals);
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
    protected boolean step(int step, int stepsThisTick) {
        boolean cancel = super.step(step, stepsThisTick);

        setStepsPerTick(getVelocity().length() * 5);

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
