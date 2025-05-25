package wbs.wandcraft.crafting;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsMath;
import wbs.wandcraft.WbsWandcraft;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@NullMarked
public class ArtificingTable {
    public static final int MAX_ITEMS = 7;
    public static final double ITEM_DISTANCE = 1.5;
    private static final NamespacedKey SPAWN_TIME = WbsWandcraft.getKey("spawn_time");

    private final Block block;

    public ArtificingTable(Block block) {
        this.block = block;
    }

    public Location getCentralItemLocation() {
        // TODO: Make the y offset configurable
        return block.getLocation().add(0.5, 1.2, 0.5);
    }

    @Nullable
    public Item getCentralItem() {
        List<Item> items = getItems();
        if (items.isEmpty()) {
            return null;
        }
        return items.getFirst();
    }

    public List<Item> getItems() {
        return block.getWorld().getNearbyEntitiesByType(
                Item.class,
                block.getLocation(),
                3, // TODO: Make this distance configurable? Related to a "distance from table" number for
                item -> item.getPersistentDataContainer().has(ArtificingConfig.TAG)
        ).stream()
                // Sort by age, oldest first -- that's the order they were added. First item is at the centre.
                .sorted(Comparator.comparing(item -> Objects.requireNonNull(
                        item.getPersistentDataContainer().get(SPAWN_TIME, PersistentDataType.LONG))
                ))
                .toList().reversed();
    }

    public int occupiedOuterSlots() {
        return Math.min(0, getItems().size() - 1);
    }

    public int getRemainingSlots() {
        return MAX_ITEMS - occupiedOuterSlots();
    }

    public boolean canAcceptItems() {
        return getItems().size() < MAX_ITEMS;
    }

    /**
     * Accept an item onto the table.
     * @param existingItem The already existing item in the world.
     * @return The affected item entity, if it still exists. Null if it was removed for any reason.
     */
    @Nullable
    public Item acceptItem(Item existingItem) {
        if (!canAcceptItems()) {
            throw new IllegalStateException("Table is not able to accept new items.");
        }

        Location centralItemLocation = getCentralItemLocation();

        // If more than 1 item provided, split out new items until the table can't hold more.
        if (existingItem.getItemStack().getAmount() > 1) {
            ItemStack stack = existingItem.getItemStack();

            int amount = stack.getAmount();
            int remainingSlots = getRemainingSlots();
            for (int i = 0; i < remainingSlots && i < amount; i++) {
                // Recurse, always with stack size == 1 to avoid looping
                existingItem.getWorld().dropItem(centralItemLocation, stack.asOne(), this::acceptItem);
            }

            int remainingInStack = amount - remainingSlots;
            if (remainingInStack > 0) {
                existingItem.setItemStack(stack.asQuantity(remainingInStack));
                return existingItem;
            } else {
                existingItem.remove();
                return null;
            }
        }

        existingItem.setGravity(false);
        existingItem.setGlowing(true);

        existingItem.setCanPlayerPickup(false);
        existingItem.setCanMobPickup(false);
        // Don't disable aging, or we can't sort by ticks lived later.
        existingItem.getPersistentDataContainer().set(SPAWN_TIME, PersistentDataType.LONG, System.currentTimeMillis());

        List<Item> currentItems = getItems();

        if (currentItems.isEmpty()) {
            existingItem.teleport(centralItemLocation);
            return existingItem;
        }

        Item centralItem = currentItems.removeFirst();

        // Can't make rotation random, or adding subsequent items will move existing ones to a random rotation too
        ArrayList<Vector> offsets = WbsMath.get2Ring(currentItems.size(), ITEM_DISTANCE, 0);

        for (int i = 0; i < currentItems.size(); i++) {
            Item item = currentItems.get(i);
            Vector offset = offsets.get(i);

            item.teleport(centralItemLocation.clone().add(offset));
        }

        tryCrafting();

        // The item may have been removed while crafting --
        if (existingItem.isValid()) {
            return existingItem;
        }

        return null;
    }

    private void tryCrafting() {
        List<Item> items = getItems();

        Item central = items.removeFirst();


    }

    public Block getBlock() {
        return block;
    }
}
