package wbs.wandcraft.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import wbs.utils.util.persistent.BlockChunkStorageUtil;
import wbs.wandcraft.objects.colliders.MagicSpawnedBlock;

import java.util.Map;

public class MagicBlockEvents implements Listener {
    @EventHandler
    public void onMagicBlockLoad(ChunkLoadEvent event) {
        Map<Block, PersistentDataContainer> map = BlockChunkStorageUtil.getBlockContainerMap(event.getChunk());

        for (Map.Entry<Block, PersistentDataContainer> entry : map.entrySet()) {
            Block block = entry.getKey();
            PersistentDataContainer container = entry.getValue();

            if (container.has(MagicSpawnedBlock.MAGIC_BLOCK)) {
                block.setType(Material.AIR);

                container.remove(MagicSpawnedBlock.MAGIC_BLOCK);
                BlockChunkStorageUtil.writeContainer(block, container);
            }
        }
    }
}
