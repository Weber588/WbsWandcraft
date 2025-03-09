package wbs.wandcraft.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import wbs.utils.util.WbsEventUtils;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.wand.WandHolder;

@SuppressWarnings("unused")
public class WandInventoryEvents implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof WandHolder holder) {
            ItemStack addedItem = WbsEventUtils.getItemAddedToTopInventory(event);
            if (addedItem != null) {
                if (holder.wand().canContain(addedItem)) {
                    WbsWandcraft.getInstance().runSync(holder::save);
                } else {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof WandHolder holder) {
            holder.save();
        }
    }
}
