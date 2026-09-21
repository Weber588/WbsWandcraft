package wbs.wandcraft.spell.effect;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Fire;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.util.RayTraceResult;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.pluginhooks.WbsRegionUtils;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.attributes.attributable.BurnTimeAttributable;

@NullMarked
public class IgniteEffect extends SpellEffectDefinition<RayTraceResult> implements BurnTimeAttributable {
    public IgniteEffect() {
        super(RayTraceResult.class, "ignite");
    }

    @Override
    public void run(CastContext context, SpellEffectInstance<RayTraceResult> effectInstance, RayTraceResult result) {
        Block hitBlock = result.getHitBlock();

        if (hitBlock != null) {
            BlockFace hitBlockFace = result.getHitBlockFace();
            if (hitBlockFace != null) {
                Block relative = hitBlock.getRelative(hitBlockFace);
                if (relative.isEmpty()) {
                    if (WbsRegionUtils.canBuildAt(relative.getLocation(), context.player())) {
                        Fire fire = (Fire) Material.FIRE.createBlockData();
                        BlockFace fireFace = hitBlockFace.getOppositeFace();
                        if (fireFace != BlockFace.DOWN) {
                            fire.setFace(fireFace, true);
                        }
                        relative.setBlockData(fire);
                    }
                }
            }
        }

        Entity hitEntity = result.getHitEntity();
        if (hitEntity instanceof Damageable damageable) {
            ignite(damageable, context);
        }
    }

    @Override
    public Component toComponent(SpellEffectInstance<RayTraceResult> instance) {
        return Component.text("Set a block/entity on fire.");
    }
}
