package wbs.wandcraft.events;

import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
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

        wand.startCasting(player, item);
    }

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
            // Don't try casting if it's a wand with a consumable component -- it needs to complete an animation first.
            if (!item.hasData(DataComponentTypes.CONSUMABLE)) {
                wand.startCasting(player, item);
            }
        }
    }
}
