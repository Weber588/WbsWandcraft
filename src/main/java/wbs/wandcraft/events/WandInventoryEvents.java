package wbs.wandcraft.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import wbs.utils.util.WbsEventUtils;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.wand.WandHolder;

@SuppressWarnings("unused")
public class WandInventoryEvents implements Listener {
    private static final boolean DEBUG = false;
    private static void debug(String message) {
        if (DEBUG) {
            WbsWandcraft.getInstance().getLogger().info(message);
        }
    }

    // TODO: Prevent click and drag from allowing items to be put in inventory unexpectedly

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
            debug("Should save after click -- checking next tick.");
            WbsWandcraft.getInstance().runSync(() -> {
                debug("Next tick!");
                // Save if still open -- if not, it's been done in the Close event
                if (event.getWhoClicked().getOpenInventory().getTopInventory().getHolder() instanceof WandHolder updatedHolder) {
                    debug("Saving!");
                    updatedHolder.save();
                }
            });
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof WandHolder holder) {
            debug("InventoryCloseEvent -- saving");
            holder.save();
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (event.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof WandHolder holder) {
            event.setCancelled(true);
        }
    }
}
