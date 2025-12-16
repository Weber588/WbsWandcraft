package wbs.wandcraft.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.crafting.ArtificingConfig;
import wbs.wandcraft.crafting.ArtificingTable;
import wbs.wandcraft.wand.types.WizardryWand;

import java.util.List;
import java.util.Objects;

public class ArtificingBlockEvents implements Listener {
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
                handleInteract(event, event.getPlayer(), table, hand);
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
            handleInteract(event, event.getPlayer(), table, hand);
        }
    }

    private static void handleInteract(Cancellable event, Player player, ArtificingTable table, @NotNull EquipmentSlot hand) {
        if (player.isSneaking()) {
            table.dropLatestItem();
            event.setCancelled(true);
        } else if (table.canAcceptItems()) {
            ItemStack heldItem = player.getInventory().getItem(hand);
            if (WizardryWand.getIfValid(heldItem) != null) {
                return;
            }

            if (!heldItem.isEmpty() && !heldItem.getPersistentDataContainer().has(ArtificingConfig.TAG)) {
                player.dropItem(hand, 1);
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
