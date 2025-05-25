package wbs.wandcraft.events;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.crafting.ArtificingConfig;

import java.util.List;

public class ArtificingBlockEvents implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (!event.getItemInHand().getPersistentDataContainer().has(ArtificingConfig.TAG)) {
            return;
        }

        WbsWandcraft.getInstance().getSettings().getArtificingConfig().placeAt(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!ArtificingConfig.isInstance(event.getBlock())) {
            return;
        }

        WbsWandcraft.getInstance().getSettings().getArtificingConfig().handleBreak(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockExplodeEvent event) {
        List<Block> blocks = event.blockList();

        blocks.stream()
                .filter(ArtificingConfig::isInstance)
                .forEach(block ->
                        WbsWandcraft.getInstance().getSettings().getArtificingConfig().handleBreak(block)
                );
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPush(BlockPistonExtendEvent event) {
        List<Block> blocks = event.getBlocks();
        if (blocks.stream().anyMatch(ArtificingConfig::isInstance)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPull(BlockPistonRetractEvent event) {
        List<Block> blocks = event.getBlocks();
        if (blocks.stream().anyMatch(ArtificingConfig::isInstance)) {
            event.setCancelled(true);
        }
    }
}
