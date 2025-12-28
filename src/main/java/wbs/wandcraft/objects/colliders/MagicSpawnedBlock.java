package wbs.wandcraft.objects.colliders;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.persistent.BlockChunkStorageUtil;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.objects.generics.MagicObject;

public class MagicSpawnedBlock extends MagicObject {
    public static final NamespacedKey MAGIC_BLOCK = WbsWandcraft.getKey("magic_block");

    public MagicSpawnedBlock(Block block, Player caster, @NotNull CastContext castContext) {
        super(block.getLocation(), caster, castContext);

        this.block = block;
        material = block.getType();
    }

    private final Block block;
    private boolean expireOnMaterialChange;
    private final Material material;
    private boolean removeBlockOnExpire;
    private boolean breakNaturally;
    private int duration;

    private int age = 0;

    @Override
    public boolean spawn() {
        if (!super.spawn()) {
            return false;
        }

        BlockChunkStorageUtil.modifyContainer(block, container ->
                container.set(MAGIC_BLOCK, PersistentDataType.BOOLEAN, true)
        );

        return true;
    }

    @Override
    protected boolean tick() {
        if (expireOnMaterialChange) {
            if (block.getType() != material) {
                return true;
            }
        }

        if (duration > 0) {
            age++;

            if (age >= duration) {
                return true;
            }
        }

        return false;
    }

    @Override
    protected void onRemove() {
        if (removeBlockOnExpire) {
            if (breakNaturally) {
                block.breakNaturally();
            } else {
                block.setType(Material.AIR);
            }
        }

        BlockChunkStorageUtil.modifyContainer(block, container ->
                container.remove(MAGIC_BLOCK)
        );
    }

    public boolean expireOnMaterialChange() {
        return expireOnMaterialChange;
    }

    public void setExpireOnMaterialChange(boolean expireOnMaterialChange) {
        this.expireOnMaterialChange = expireOnMaterialChange;
    }

    public boolean removeBlockOnExpire() {
        return removeBlockOnExpire;
    }

    public void setRemoveBlockOnExpire(boolean removeBlockOnExpire) {
        this.removeBlockOnExpire = removeBlockOnExpire;
    }

    public boolean breakNaturally() {
        return breakNaturally;
    }

    public void setBreakNaturally(boolean breakNaturally) {
        this.breakNaturally = breakNaturally;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public Block getBlock() {
        return block;
    }

    public int getDuration() {
        return duration;
    }
}
