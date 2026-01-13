package wbs.wandcraft.ai;

import com.destroystokyo.paper.entity.ai.GoalKey;
import com.destroystokyo.paper.entity.ai.GoalType;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.effects.StatusEffectManager;

import java.util.EnumSet;
import java.util.List;

@NullMarked
public class CustomAvoidGoal extends TemporaryGoal<Mob> {
    private final RadiusSelector<LivingEntity> selector;
    private final double movementModifier;

    public CustomAvoidGoal(Mob mob, double movementModifier) {
        super(mob);
        this.selector = new RadiusSelector<>(LivingEntity.class)
                .setRange(25)
                .setPredicate(check -> StatusEffectManager.getInstance(check, StatusEffectManager.INVISIBLE) == null)
                .exclude(mob);
        this.movementModifier = movementModifier;
    }

    public CustomAvoidGoal(Mob mob, RadiusSelector<LivingEntity> selector, double movementModifier) {
        super(mob);
        this.selector = selector.exclude(mob);
        this.movementModifier = movementModifier;
    }

    @Override
    public boolean shouldActivate() {
        List<LivingEntity> nearbyEntities = selector.select(mob);

        return !nearbyEntities.isEmpty();
    }

    @Override
    public void onTick() {
        List<LivingEntity> nearbyEntities = selector.select(mob);
        if (nearbyEntities.isEmpty()) {
            return;
        }

        LivingEntity nearest = nearbyEntities.getFirst();

        Location nearestLocation = nearest.getLocation();

        Vector offset = mob.getLocation().subtract(nearestLocation).toVector();

        Location targetLocation = mob.getLocation().add(offset);

        boolean inWater = WbsEntityUtil.isInWater(mob);

        if (Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
            Disguise disguise = DisguiseAPI.getDisguise(mob);
            if (disguise != null) {
                disguise.getWatcher().setSwimming(inWater);
            }
        }

        //noinspection deprecation
        mob.setSwimming(inWater);

        if (inWater) {
            targetLocation.setY(mob.getY());
            mob.lookAt(targetLocation.clone().add(0, 1, 0));
        } else {
            Block safeLocation = WbsEntityUtil.getSafeLocation(mob, targetLocation, 5, new Vector(0, -1, 0));
            if (safeLocation == null) {
                safeLocation = WbsEntityUtil.getSafeLocation(mob, targetLocation, 5, new Vector(0, 1, 0));
            }

            if (safeLocation != null) {
                targetLocation = safeLocation.getLocation();
            }
            mob.lookAt(targetLocation);
        }

        mob.getPathfinder().moveTo(targetLocation, movementModifier);
    }

    @Override
    public GoalKey<Mob> getKey() {
        return GoalKey.of(Mob.class, WbsWandcraft.getKey("avoid_everything"));
    }

    @Override
    public EnumSet<GoalType> getTypes() {
        return EnumSet.of(GoalType.MOVE);
    }
}
