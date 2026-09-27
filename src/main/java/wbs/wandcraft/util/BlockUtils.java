package wbs.wandcraft.util;

import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys;
import org.bukkit.block.BlockType;
import org.jetbrains.annotations.Contract;
import wbs.utils.util.WbsRegistryUtil;

import java.util.Set;

public class BlockUtils {
    private static final Set<BlockType> OVERRIDE_BLOCKS = Set.of(
            BlockType.COBBLESTONE, BlockType.END_STONE, BlockType.COBBLED_DEEPSLATE, BlockType.GRASS_BLOCK
    );

    @Contract("null -> false")
    public static boolean isNatureGroundBlock(BlockType blockType) {
        if (blockType == null) {
            return false;
        }

        if (OVERRIDE_BLOCKS.contains(blockType)) {
            return true;
        }

        boolean matched = WbsRegistryUtil.isTagged(blockType, BlockTypeTagKeys.BASE_STONE_OVERWORLD);
        matched |= WbsRegistryUtil.isTagged(blockType, BlockTypeTagKeys.DIRT);
        matched |= blockType.key().value().toLowerCase().endsWith("_ore");

        return matched;
    }
}
