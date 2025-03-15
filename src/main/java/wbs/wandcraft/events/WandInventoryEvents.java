package wbs.wandcraft.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import wbs.utils.util.WbsEventUtils;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.wand.WandHolder;

@SuppressWarnings("unused")
public class WandInventoryEvents implements Listener {
    @EventHandler(priority= EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof WandHolder holder) {
            ItemStack addedItem = WbsEventUtils.getItemAddedToTopInventory(event);
            if (addedItem != null) {
                if (!holder.wand().canContain(addedItem)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority= EventPriority.MONITOR, ignoreCancelled = true)
    public void monitorInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof WandHolder holder) {
            WbsWandcraft.getInstance().runSync(holder::save);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof WandHolder holder) {
            holder.save();
        }
    }
}
