package wbs.wandcraft.events;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Random;

public class RecipeEvents implements Listener {
    public static final int XP_PER_SHARD = 5;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.recipeIterator().forEachRemaining(recipe -> {
            if (recipe instanceof Keyed keyed) {
                if (keyed.key().namespace().equals("wbswandcraft")) {
                    player.discoverRecipe(keyed.getKey());
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAmethystSculk(BlockSpreadEvent event) {
        Block overtaken = event.getBlock();
        BlockState newState = event.getNewState();

        if (overtaken.getType() == Material.AMETHYST_BLOCK && newState.getType() == Material.SCULK) {
            overtaken.getWorld().dropItemNaturally(
                    overtaken.getLocation().toCenterLocation().add(0, 0.51, 0),
                    ItemStack.of(Material.ECHO_SHARD, new Random().nextInt(3) + 1)
            );
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        World world = entity.getWorld();

        if (entity instanceof Monster monster) {
            List<Entity> nearbyEntities = monster.getNearbyEntities(3, 3, 3);

            int droppedExp = event.getDroppedExp();

            if (droppedExp >= XP_PER_SHARD) {
                int toConvert = droppedExp / XP_PER_SHARD;

                List<Item> itemEntities = nearbyEntities.stream()
                        .filter(Item.class::isInstance)
                        .map(Item.class::cast)
                        .filter(item -> item.getItemStack().getType() == Material.AMETHYST_SHARD)
                        .toList();
                int converted = 0;
                for (Item itemEntity : itemEntities) {
                    if (converted >= toConvert) {
                        break;
                    }
                    ItemStack stack = itemEntity.getItemStack();
                    if (stack.getAmount() <= toConvert - converted) {
                        itemEntity.setItemStack(stack.withType(Material.ECHO_SHARD));
                        converted += stack.getAmount();
                    } else {
                        int convertedThisEntity = 0;
                        for (int i = 0; i < toConvert - converted && stack.getAmount() > 0; i++) {
                            convertedThisEntity++;
                            world.dropItem(itemEntity.getLocation(), stack.asOne().withType(Material.ECHO_SHARD));
                            world.spawnParticle(Particle.SCULK_SOUL, itemEntity.getLocation(), 1);
                            stack.setAmount(stack.getAmount() - 1);
                        }
                        itemEntity.setItemStack(stack);
                        converted += convertedThisEntity;
                    }
                }

                if (converted > 0) {
                    event.setDroppedExp(droppedExp - (XP_PER_SHARD * converted));
                }
            }
        }
    }
}
