package wbs.wandcraft.crafting;

import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsMath;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.wandcraft.WbsWandcraft;

import java.util.*;
import java.util.stream.Collectors;

@NullMarked
public class ArtificingTable {
    public static final int MAX_ITEMS = 7;
    public static final double ITEM_DISTANCE = 1.5;
    private static final NamespacedKey SPAWN_TIME = WbsWandcraft.getKey("spawn_time");

    private static final Team COLOUR_TEAM = Bukkit.getScoreboardManager()
            .getMainScoreboard()
            .registerNewTeam("test");

    static {
        // TODO: Make this colour configurable
        COLOUR_TEAM.color(NamedTextColor.AQUA);
    }

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
        Collection<Item> nearbyOnTable = block.getWorld().getNearbyEntitiesByType(
                Item.class,
                block.getLocation(),
                3,
                ArtificingConfig::isArtificingItem
        );

        // Sort by age, oldest first -- that's the order they were added. First item is at the centre.
        return nearbyOnTable.stream()
                .sorted(Comparator.comparing(item -> Objects.requireNonNull(
                        item.getPersistentDataContainer().get(SPAWN_TIME, PersistentDataType.LONG))
                ))
                .collect(Collectors.toCollection(LinkedList::new));
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
     * @param spawningItem The already existing item in the world.
     * @return The affected item entity, if it still exists. Null if it was removed for any reason.
     */
    @Nullable
    public Item acceptItem(Item spawningItem) {
        if (!canAcceptItems()) {
            throw new IllegalStateException("Table is not able to accept new items.");
        }

        Location centralItemLocation = getCentralItemLocation();

        // If more than 1 item provided, split out new items until the table can't hold more.
        if (spawningItem.getItemStack().getAmount() > 1) {
            ItemStack stack = spawningItem.getItemStack();

            int amount = stack.getAmount();
            int remainingSlots = getRemainingSlots();
            for (int i = 0; i < remainingSlots && i < amount; i++) {
                // Recurse, always with stack size == 1 to avoid looping
                spawningItem.getWorld().dropItem(centralItemLocation, stack.asOne());
            }

            int remainingInStack = amount - remainingSlots;
            if (remainingInStack > 0) {
                spawningItem.setItemStack(stack.asQuantity(remainingInStack));
                return spawningItem;
            } else {
                spawningItem.remove();
                return null;
            }
        }

        spawningItem.setGravity(false);
        spawningItem.setGlowing(true);
        COLOUR_TEAM.addEntities(spawningItem);

        spawningItem.setVelocity(new Vector());

        spawningItem.setCanPlayerPickup(false);
        spawningItem.setCanMobPickup(false);
        spawningItem.setWillAge(false);

        PersistentDataContainer container = spawningItem.getPersistentDataContainer();
        container.set(SPAWN_TIME, PersistentDataType.LONG, System.currentTimeMillis());
        container.set(ArtificingConfig.TAG, WbsPersistentDataType.NAMESPACED_KEY, ArtificingConfig.getBlockKey(block));

        List<Item> currentItems = getItems();

        if (currentItems.isEmpty()) {
            spawningItem.teleport(centralItemLocation);
            return spawningItem;
        }

        Item centralItem = currentItems.removeFirst();
        currentItems.add(spawningItem);

        // Can't make rotation random, or adding subsequent items will move existing ones to a random rotation too
        ArrayList<Vector> offsets = WbsMath.get2Ring(currentItems.size(), ITEM_DISTANCE, 0);

        for (int i = 0; i < currentItems.size(); i++) {
            Item item = currentItems.get(i);
            Vector offset = offsets.get(i);

            Location itemLoc = centralItemLocation.clone().add(offset);
            item.teleport(itemLoc);
            item.getWorld().getNearbyPlayers(item.getLocation(), 32)
                    .forEach(player -> {
                        // Hide and immediately show the entity to nearby players -- this forces packets to instantly update their positions.
                        player.hideEntity(WbsWandcraft.getInstance(), item);
                        player.showEntity(WbsWandcraft.getInstance(), item);
                    });
        }

        tryCrafting();

        // The item may have been removed while crafting --
        if (spawningItem.isValid()) {
            return spawningItem;
        }

        return null;
    }

    private void tryCrafting() {
        List<Item> items = getItems();

        Item central = items.removeFirst();


    }

    public void breakTable() {
        ArtificingConfig.unregisterTable(this);

        NamespacedKey blockKey = ArtificingConfig.getBlockKey(block);

        block.getWorld().getNearbyEntities(
                BoundingBox.of(block).expand(32),
                entity -> entity.getPersistentDataContainer().has(blockKey)
        ).forEach(Entity::remove);

        block.getWorld().getNearbyEntities(
                        BoundingBox.of(block).expand(32),
                        entity -> Objects.equals(
                                entity.getPersistentDataContainer().get(ArtificingConfig.TAG, WbsPersistentDataType.NAMESPACED_KEY),
                                blockKey
                        )
                ).stream()
                .map(entity -> {
                    if (entity instanceof Item item) {
                        return item;
                    }
                    return null;
                }).filter(Objects::nonNull)
                .forEach(item -> {
                    ItemStack stack = item.getItemStack();
                    item.getWorld().dropItem(item.getLocation(), stack);
                    item.remove();
                });

        block.setBlockData(Material.AIR.createBlockData());
    }

    public Block getBlock() {
        return block;
    }
}
