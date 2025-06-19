package wbs.wandcraft.events;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import wbs.wandcraft.crafting.ArtificingConfig;
import wbs.wandcraft.crafting.ArtificingTable;

import java.util.Comparator;
import java.util.Optional;

public class ArtificingItemEvents implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDropNearTable(PlayerDropItemEvent event) {
        Item itemEntity = event.getItemDrop();
        ItemStack item = itemEntity.getItemStack();

        if (item.getPersistentDataContainer().has(ArtificingConfig.TAG)) {
            return;
        }

        Optional<ArtificingTable> closest = ArtificingConfig.getNearbyTables(itemEntity.getLocation(), 5).stream()
                .max(Comparator.comparingDouble(table ->
                        table.getBlock().getLocation().distanceSquared(itemEntity.getLocation())
                ));

        if (closest.isEmpty()) {
            return;
        }

        ArtificingTable table = closest.get();

        if (table.canAcceptItems()) {
            table.acceptItem(itemEntity);
        }
    }
}
