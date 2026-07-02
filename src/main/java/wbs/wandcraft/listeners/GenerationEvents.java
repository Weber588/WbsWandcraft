
package wbs.wandcraft.listeners;

import java.util.List;
import java.util.Map;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import wbs.wandcraft.WandcraftSettings;
import wbs.wandcraft.WbsWandcraft;

public class GenerationEvents implements Listener {
    public GenerationEvents() {
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        WandcraftSettings settings = WbsWandcraft.getInstance().getSettings();
        Map<EntityType, Map<ItemStack, Double>> customDrops = settings.getCustomDrops();
        Map<ItemStack, Double> itemDropChances = customDrops.get(event.getEntityType());

        if (itemDropChances != null) {
            List<ItemStack> drops = event.getDrops();
            itemDropChances.forEach((item, chance) -> {
                double random = Math.random();
                if (random < chance) {
                    drops.add(item);
                }
            });
        }
    }
}