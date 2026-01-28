package wbs.wandcraft.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Interaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import wbs.utils.util.WbsEventUtils;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.crafting.ArtificingConfig;
import wbs.wandcraft.crafting.ArtificingTable;

import java.util.List;
import java.util.Objects;

public class ArtificingTableEvents implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent event) {
        if (!event.getItemInHand().getPersistentDataContainer().has(ArtificingConfig.TAG)) {
            return;
        }

        WbsWandcraft.getInstance().getSettings().getArtificingConfig().placeAt(event.getBlock());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        ArtificingTable table = ArtificingConfig.getTable(event.getBlock());
        if (table != null) {
            // TODO: Make ability to break the table configurable
            table.breakTable();
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPunchInteraction(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Interaction interaction)) {
            return;
        }

        ArtificingTable table = ArtificingConfig.getTable(interaction);

        if (table != null) {
            // TODO: Make ability to break the table configurable
            table.breakTable();
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractBlock(PlayerInteractEvent event) {
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        ArtificingTable table = ArtificingConfig.getTable(clickedBlock);
        if (table != null) {
            EquipmentSlot hand = event.getHand();
            if (hand != null) {
                if (event.getAction().isRightClick()) {
                    table.interact(event.getPlayer(), hand);
                } else {
                    table.breakTable();
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInteractInteraction(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Interaction interaction)) {
            return;
        }

        ArtificingTable table = ArtificingConfig.getTable(interaction);
        EquipmentSlot hand = event.getHand();

        if (table != null) {
            table.interact(event.getPlayer(), hand);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onCloseInventory(InventoryCloseEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();

        if (holder instanceof ArtificingTable table) {
            table.save();
        }
    }

    @EventHandler
    public void onCloseInventory(InventoryClickEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();

        if (holder instanceof ArtificingTable) {
            ItemStack added = WbsEventUtils.getItemAddedToTopInventory(event);

            if (added != null && added.getType() != Material.ECHO_SHARD) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBreak(BlockExplodeEvent event) {
        List<Block> blocks = event.blockList();

        // TODO: Make ability to break the table configurable
        blocks.stream()
                .map(ArtificingConfig::getTable)
                .filter(Objects::nonNull)
                .forEach(ArtificingTable::breakTable);
        event.setCancelled(true);
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
