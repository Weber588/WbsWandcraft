package wbs.wandcraft.events;

import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.crafting.ArtificingConfig;
import wbs.wandcraft.wand.Wand;

@SuppressWarnings("unused")
public class WandEvents implements Listener {
    @EventHandler
    public void onConsumeWand(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();

        Wand wand = Wand.getIfValid(item);
        if (wand == null) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();

        // Crafting, not crafter or workbench -- represents the internal player inventory. Returned when nothing open.
        InventoryType inventoryType = player.getOpenInventory().getType();
        if (inventoryType != InventoryType.CRAFTING && inventoryType != InventoryType.CREATIVE) {
            return;
        }

        // TODO: Add field to Wand "being_edited" that allows stateful tracking of when casting is possible, to
        //  avoid lag issues allowing players to cast while in the wand's menu.
        wand.tryCasting(player, item);
    }

    @EventHandler
    public void onWandClick(PlayerInteractEvent event) {
        WbsWandcraft plugin = WbsWandcraft.getInstance();

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        // Crafting, not crafter or workbench -- represents the internal player inventory. Returned when nothing open.
        InventoryType inventoryType = player.getOpenInventory().getType();
        if (inventoryType != InventoryType.CRAFTING && inventoryType != InventoryType.CREATIVE) {
            return;
        }

        Wand wand = Wand.getIfValid(item);
        if (wand == null) {
            return;
        }

        Block clicked = event.getClickedBlock();
        if (clicked != null && ArtificingConfig.isInstance(clicked)) {
            player.openInventory(wand.getInventory(item).getInventory());
            player.clearActiveItem();
        } else {
            // Don't try casting if it's a wand with a consumable component -- it needs to complete an animation first.
            if (!item.hasData(DataComponentTypes.CONSUMABLE)) {
                wand.tryCasting(player, item);
            }
        }
    }

    @EventHandler
    public void onWandClick(PlayerInteractEntityEvent event) {
        WbsWandcraft plugin = WbsWandcraft.getInstance();

        Player player = event.getPlayer();
        EquipmentSlot hand = event.getHand();
        ItemStack item = player.getInventory().getItem(hand);

        // Crafting, not crafter or workbench -- represents the internal player inventory. Returned when nothing open.
        InventoryType inventoryType = player.getOpenInventory().getType();
        if (inventoryType != InventoryType.CRAFTING && inventoryType != InventoryType.CREATIVE) {
            return;
        }

        Wand wand = Wand.getIfValid(item);
        if (wand == null) {
            return;
        }

        Entity clicked = event.getRightClicked();
        if (clicked instanceof Interaction interaction && ArtificingConfig.getTable(interaction) != null) {
            player.openInventory(wand.getInventory(item).getInventory());
            player.clearActiveItem();
        }
    }
}
