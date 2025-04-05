package wbs.wandcraft.events;

import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import wbs.wandcraft.WbsWandcraft;
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
        if (player.isSneaking()) {
            return;
        }

        // Crafting, not crafter or workbench -- represents the internal player inventory. Returned when nothing open.
        InventoryType inventoryType = player.getOpenInventory().getType();
        if (inventoryType != InventoryType.CRAFTING && inventoryType != InventoryType.CREATIVE) {
            return;
        }

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

        if (event.getAction().isRightClick() && player.isSneaking()) {
            player.openInventory(wand.getInventory(item).getInventory());
            player.clearActiveItem();
        } else {
            // Don't try casting if it's a wand with a consumable component -- it needs to complete an animation first.
            if (!item.hasData(DataComponentTypes.CONSUMABLE)) {
                wand.tryCasting(player, item);
            }
        }
    }
}
