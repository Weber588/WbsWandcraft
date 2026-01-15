package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.ISpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.event.SpellTriggeredEvents;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public interface RaySpell extends ISpellDefinition, CastableSpell, RangedSpell, RadiusedSpell, DirectionalSpell {
    @Override
    default void cast(CastContext context) {
        if (getStepSize() <= 0) {
            throw new IllegalStateException("getStepSize() must return a positive value.");
        }

        Player player = context.player();
        SpellInstance instance = context.instance();

        double range = instance.getAttribute(RANGE);

        RayTraceResult endResult = player.getWorld().rayTraceBlocks(player.getEyeLocation(), WbsMath.getFacingVector(player), range, getFluidCollisionMode(), true);

        Block hitBlock = null;

        if (endResult != null) {
            hitBlock = endResult.getHitBlock();
        }

        Location endLoc;
        if (hitBlock == null) {
            endLoc = context.location().add(getDirection(context, range));
        } else {
            endLoc = endResult.getHitPosition().toLocation(player.getWorld());
        }

        double distance = endLoc.distance(context.location());

        Vector direction = getDirection(context, getStepSize());
        Location currentPos = context.location();

        Set<LivingEntity> alreadyHit = new HashSet<>();

        double radius = instance.getAttribute(RADIUS);

        RadiusSelector<LivingEntity> radiusTargeter = new RadiusSelector<>(LivingEntity.class)
                .setRange(radius);

        int maxSteps = (int) (distance / getStepSize());
        for (int currentStep = 0; currentStep <= maxSteps; currentStep++) {
            currentPos.add(direction);

            if (canHitEntities()) {
                List<LivingEntity> hit = radiusTargeter.selectExcluding(currentPos, player);
                hit.removeAll(alreadyHit);
                hit.remove(player.getPlayer());
                for (LivingEntity target : hit) {
                    RayTraceResult result = new RayTraceResult(WbsEntityUtil.getMiddleLocation(target).toVector(), target);
                    context.runEffects(SpellTriggeredEvents.ON_HIT_TRIGGER, result);
                    onHitEntity(context, currentPos, target);
                }
                alreadyHit.addAll(hit);
            }

            if (onStep(context, currentPos, alreadyHit, currentStep, maxSteps)) {
                return;
            }
        }

        if (hitBlock != null) {
            onHitBlock(context, hitBlock, Objects.requireNonNull(endResult.getHitBlockFace()));
        }
    }

    default void onHitBlock(CastContext context, @NotNull Block hitBlock, @NotNull BlockFace hitBlockFace) {

    }

    default @NotNull FluidCollisionMode getFluidCollisionMode() {
        return FluidCollisionMode.NEVER;
    }

    default void onHitEntity(CastContext context, Location currentPos, LivingEntity target) {

    }

    boolean onStep(CastContext context, Location currentPos, Set<LivingEntity> alreadyHit, int currentStep, int maxSteps);
    boolean canHitEntities();
    double getStepSize();
}
