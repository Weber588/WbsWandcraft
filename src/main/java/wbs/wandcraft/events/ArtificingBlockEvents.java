package wbs.wandcraft.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Interaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.crafting.ArtificingConfig;
import wbs.wandcraft.crafting.ArtificingTable;

import java.util.List;
import java.util.Objects;

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
        ArtificingTable table = ArtificingConfig.getTable(event.getBlock());
        if (table != null) {
            // TODO: Make ability to break the table configurable
            table.breakTable();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPunchInteraction(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Interaction interaction)) {
            return;
        }

        ArtificingTable table = ArtificingConfig.getTable(interaction);

        if (table != null) {
            // TODO: Make ability to break the table configurable
            table.breakTable();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockExplodeEvent event) {
        List<Block> blocks = event.blockList();

        // TODO: Make ability to break the table configurable
        blocks.stream()
                .map(ArtificingConfig::getTable)
                .filter(Objects::nonNull)
                .forEach(ArtificingTable::breakTable);
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
