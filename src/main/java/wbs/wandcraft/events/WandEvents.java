package wbs.wandcraft.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

        Wand wand = Wand.getIfValid(item);
        if (wand == null) {
            return;
        }

        Player player = event.getPlayer();
        if (event.getAction().isRightClick() && player.isSneaking()) {
            player.openInventory(wand.getInventory(item).getInventory());
        } else {
            wand.cast(player, item);
        }
    }
}
