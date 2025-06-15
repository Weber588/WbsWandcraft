package wbs.wandcraft.crafting;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.configuration.WbsConfigReader;
import wbs.wandcraft.WandcraftSettings;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.util.ConfiguredBlockDisplay;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class ArtificingConfig {
    public static final NamespacedKey TAG = WbsWandcraft.getKey("artificing_table");

    public static NamespacedKey getBlockKey(Block block) {
        return WbsWandcraft.getKey(block.getX() + "_" + block.getY() + '_' + block.getZ());
    }

    public static Location locationFromKey(NamespacedKey key) {
        String asString = key.value();

        String[] args = asString.split("_");
        if (args.length != 3) {
            throw new IllegalStateException("Invalid block key: " + key.asString());
        }

        return new Location(null, Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
    }

    public static boolean isInstance(Block block) {
        PersistentDataContainer artificingContainer = block.getChunk().getPersistentDataContainer().get(TAG, PersistentDataType.TAG_CONTAINER);

        if (artificingContainer == null || !artificingContainer.has(getBlockKey(block))) {
            return false;
        }

        return true;
    }

    public static ArtificingTable getTable(Block block) {
        PersistentDataContainer artificingContainer = block.getChunk().getPersistentDataContainer().get(TAG, PersistentDataType.TAG_CONTAINER);

        if (artificingContainer == null || !artificingContainer.has(getBlockKey(block))) {
            return null;
        }

        return new ArtificingTable(block);
    }

    @Nullable
    public static ArtificingTable getTable(Interaction interaction) {
        Set<NamespacedKey> keys = interaction.getPersistentDataContainer().getKeys();
        for (NamespacedKey key : keys) {
            try {
                Location location = ArtificingConfig.locationFromKey(key);

                if (ArtificingConfig.isInstance(interaction.getWorld().getBlockAt(location))) {
                    return getTable(interaction.getWorld().getBlockAt(location));
                }
            } catch (IllegalArgumentException ignored) {}
        }

        return null;
    }

    public static List<ArtificingTable> getNearbyTables(Location location, double maxDistance) {
        Chunk chunk = location.getChunk();
        World world = chunk.getWorld();

        List<ArtificingTable> tablesInChunks = new LinkedList<>(getTablesInChunk(chunk));

        // get adjacent chunks (including diagonals) and check all their persistent artificing table tags
        List<Chunk> adjacentChunks = List.of(
                world.getChunkAt(chunk.getX() + 1, chunk.getZ()),
                world.getChunkAt(chunk.getX() - 1, chunk.getZ()),
                world.getChunkAt(chunk.getX(), chunk.getZ() + 1),
                world.getChunkAt(chunk.getX(), chunk.getZ() - 1),
                world.getChunkAt(chunk.getX() + 1, chunk.getZ() + 1),
                world.getChunkAt(chunk.getX() - 1, chunk.getZ() - 1),
                world.getChunkAt(chunk.getX() - 1, chunk.getZ() + 1),
                world.getChunkAt(chunk.getX() + 1, chunk.getZ() - 1)
        );

        for (Chunk adjacent : adjacentChunks) {
            tablesInChunks.addAll(getTablesInChunk(adjacent));
        }

        tablesInChunks.removeIf(table -> location.distanceSquared(table.getBlock().getLocation()) > maxDistance * maxDistance);

        return tablesInChunks;
    }

    public static List<ArtificingTable> getTablesInChunk(Chunk chunk) {
        PersistentDataContainer chunkContainer = chunk.getPersistentDataContainer().get(TAG, PersistentDataType.TAG_CONTAINER);

        List<ArtificingTable> tables = new LinkedList<>();
        if (chunkContainer == null) {
            return tables;
        }

        World world = chunk.getWorld();

        Set<NamespacedKey> keys = chunkContainer.getKeys();
        for (NamespacedKey key : keys) {
            Location loc = locationFromKey(key);

            tables.add(getTable(world.getBlockAt(loc)));
        }

        return tables;
    }

    public static boolean isArtificingItem(Item item) {
        return item.getPersistentDataContainer().has(ArtificingConfig.TAG);
    }

    @NotNull
    private final Component name;
    @NotNull
    private final BlockData blockData;
    @NotNull
    private final ItemType itemType;
    @NotNull
    private final ItemType itemModel;
    @NotNull
    private final Set<ConfiguredBlockDisplay> blockDisplaySet = new HashSet<>();

    public ArtificingConfig(ConfigurationSection section, WandcraftSettings settings, String directory) {
        Component name = section.getComponent("name", MiniMessage.miniMessage());

        if (name == null) {
            name = Component.text("Artificing Table");
        }

        this.name = name;

        String blockDataString = section.getString("block");

        BlockData blockData;
        if (blockDataString == null) {
            settings.logError("\"block\" is a required field.", directory + "/block");
            blockData = Bukkit.createBlockData("minecraft:piston_head[facing=up]");
        } else {
            try {
                blockData = Bukkit.createBlockData(blockDataString);
            } catch (IllegalArgumentException ex) {
                settings.logError("Invalid block data string: \"" + blockDataString + "\".", directory + "/block");
                blockData = Bukkit.createBlockData("minecraft:piston_head[facing=up]");
            }
        }

        this.blockData = blockData;

        itemType = WbsConfigReader.getRegistryEntry(section, "item", RegistryKey.ITEM, ItemType.REINFORCED_DEEPSLATE);
        itemModel = WbsConfigReader.getRegistryEntry(section, "item-model", RegistryKey.ITEM, itemType);

        ConfigurationSection blockDisplaysSection = section.getConfigurationSection("block-displays");
        if (blockDisplaysSection != null) {
            for (String blockDisplayKey : blockDisplaysSection.getKeys(false)) {
                ConfigurationSection displaySection = Objects.requireNonNull(blockDisplaysSection.getConfigurationSection(blockDisplayKey));

                ConfiguredBlockDisplay display = getConfiguredBlockDisplay(settings, directory, displaySection);
                if (display == null) continue;
                blockDisplaySet.add(display);
            }
        }
    }

    private static @Nullable ConfiguredBlockDisplay getConfiguredBlockDisplay(WandcraftSettings settings, String directory, ConfigurationSection displaySection) {
        BlockData blockData;
        String displayName = displaySection.getName();

        String displayBlockDataString = displaySection.getString("block");

        if (displayBlockDataString == null) {
            settings.logError("\"block\" is a required field.", directory + "/block-displays/" + displayName + "/block");
            return null;
        }

        try {
            blockData = Bukkit.createBlockData(displayBlockDataString);
        } catch (IllegalArgumentException ex) {
            settings.logError("Invalid block data string: \"" + displayBlockDataString + "\".", directory + "/block-displays/" + displayName + "/block");
            return null;
        }

        Vector offset = WbsConfigReader.getVector(displaySection, "offset", new Vector(0, 0, 0));
        Vector scale = WbsConfigReader.getVector(displaySection, "scale", new Vector(1, 1, 1));

        ConfiguredBlockDisplay display = new ConfiguredBlockDisplay(displayName, blockData, offset, scale);
        return display;
    }

    public static void unregisterTable(ArtificingTable artificingTable) {
        Block block = artificingTable.getBlock();
        PersistentDataContainer chunkContainer = block.getChunk().getPersistentDataContainer();
        PersistentDataContainer container = chunkContainer.get(TAG, PersistentDataType.TAG_CONTAINER);

        NamespacedKey blockKey = getBlockKey(block);
        if (container != null) {
            container.remove(blockKey);
            chunkContainer.set(TAG, PersistentDataType.TAG_CONTAINER, container);
        }
    }

    public void placeAt(Block block) {
        block.setBlockData(blockData);

        PersistentDataContainer chunkContainer = block.getChunk().getPersistentDataContainer();
        PersistentDataContainer container = chunkContainer.get(TAG, PersistentDataType.TAG_CONTAINER);
        if (container == null) {
            container = chunkContainer.getAdapterContext().newPersistentDataContainer();
        }

        NamespacedKey blockKey = getBlockKey(block);
        container.set(blockKey, PersistentDataType.BOOLEAN, true);
        chunkContainer.set(TAG, PersistentDataType.TAG_CONTAINER, container);

        Location centerBottomBlock = block.getLocation().add(0.5, 0, 0.5);
        blockDisplaySet.forEach(display -> display.spawn(centerBottomBlock, blockKey));

        centerBottomBlock.getWorld().spawn(centerBottomBlock, Interaction.class, interaction -> {
            interaction.getPersistentDataContainer().set(blockKey, PersistentDataType.STRING, "interaction");
            // TODO: Make this configurable
            interaction.setInteractionWidth(1.005f);
        });
    }

    public ItemStack getItem() {
        ItemStack itemStack = itemType.createItemStack();

        itemStack.getDataTypes().forEach(itemStack::unsetData);
        itemStack.setData(DataComponentTypes.ITEM_NAME, name);
        itemStack.setData(DataComponentTypes.ITEM_MODEL, itemModel.getKey());

        itemStack.editMeta(meta -> {
            meta.getPersistentDataContainer().set(TAG, PersistentDataType.BOOLEAN, true);
        });

        return itemStack;
    }
}
