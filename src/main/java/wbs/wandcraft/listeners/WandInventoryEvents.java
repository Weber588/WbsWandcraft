package wbs.wandcraft.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.wand.WandHolder;
import wbs.wandcraft.wand.types.WizardryWandHolder;

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
        if (event.getView().getTopInventory().getHolder() instanceof WandHolder<?> holder) {
            holder.handleClick(event);
        }
    }

    @EventHandler(priority= EventPriority.MONITOR, ignoreCancelled = true)
    public void monitorInventoryClick(InventoryClickEvent event) {
        if (event.getInventory().getHolder() instanceof WandHolder<?> holder) {
            debug("Should save after click -- checking next tick.");
            holder.saveNextTick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof WizardryWandHolder holder) {
            debug("InventoryCloseEvent -- saving");
            holder.save();
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (event.getPlayer().getOpenInventory().getTopInventory().getHolder() instanceof WizardryWandHolder holder) {
            event.setCancelled(true);
        }
    }
}
