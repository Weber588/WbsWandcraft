package wbs.wandcraft.spell.effect;

import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.RayTraceResult;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.pluginhooks.WbsRegionUtils;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.attributes.attributable.BurnTimeAttributable;

@NullMarked
public class GrowEffect extends SpellEffectDefinition<RayTraceResult> implements BurnTimeAttributable {
    public GrowEffect() {
        super(RayTraceResult.class, "grow");
    }

    @Override
    public void run(CastContext context, SpellEffectInstance<RayTraceResult> effectInstance, RayTraceResult result) {
        Block hitBlock = result.getHitBlock();

        if (hitBlock != null) {
            BlockFace hitBlockFace = result.getHitBlockFace();
            if (hitBlockFace != null) {
                if (WbsRegionUtils.canBuildAt(hitBlock.getLocation(), context.player())) {
                    hitBlock.applyBoneMeal(hitBlockFace);
                }
            }
        }
    }

    @Override
    public Component toComponent(SpellEffectInstance<RayTraceResult> instance) {
        return Component.text("Set a block/entity on fire.");
    }
}
