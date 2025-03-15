package wbs.wandcraft.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import wbs.wandcraft.wand.Wand;

@SuppressWarnings("unused")
public class WandEvents implements Listener {

    @EventHandler
    public void onWandClick(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        Player player = event.getPlayer();
        // Crafting, not crafter or workbench -- represents the internal player inventory. Returned when not open.
        if (player.getOpenInventory().getType() == InventoryType.CRAFTING) {
            return;
        }

        Wand wand = Wand.getIfValid(item);
        if (wand == null) {
            return;
        }

        if (event.getAction().isRightClick() && player.isSneaking()) {
            player.openInventory(wand.getInventory(item).getInventory());
        } else {
            wand.startCasting(player, item);
        }
    }
}
