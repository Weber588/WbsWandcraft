package wbs.wandcraft.wand;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsRegistry;
import wbs.wandcraft.WbsWandcraft;

import java.util.function.Function;

@SuppressWarnings("unused")
public class WandInventoryType implements Keyed {
    // Deferred initialisation so we can register statically
    public static final WbsRegistry<WandInventoryType> WAND_INVENTORY_TYPES = new WbsRegistry<>();

    public static final WandInventoryType LINE_5 = new WandInventoryType(
            "line_5",
            wand -> Bukkit.createInventory(wand, InventoryType.HOPPER, Component.text("Inventory")),
            1,
            5
    );
    public static final WandInventoryType LINE_9 = new WandInventoryType(
            "line_9",
            wand -> Bukkit.createInventory(wand, 9, Component.text("Inventory")),
            1,
            9
    );
    public static final WandInventoryType PLANE_3x3 = new WandInventoryType(
            "plane_3_3",
            wand -> Bukkit.createInventory(wand, InventoryType.DISPENSER, Component.text("Inventory")),
            3,
            3
    );
    public static final WandInventoryType PLANE_2x9 = new WandInventoryType(
            "plane_2_9",
            wand -> Bukkit.createInventory(wand, 2 * 9, Component.text("Inventory")),
            2,
            9
    );

    private final NamespacedKey key;
    private final Function<WandHolder, Inventory> inventoryProducer;
    private final int rows;
    private final int columns;

    public WandInventoryType(String key, Function<WandHolder, Inventory> inventoryProducer, int rows, int columns) {
        this(WbsWandcraft.getKey(key), inventoryProducer, rows, columns);
    }
    public WandInventoryType(NamespacedKey key, Function<WandHolder, Inventory> inventoryProducer, int rows, int columns) {
        this.key = key;
        this.inventoryProducer = inventoryProducer;
        this.rows = rows;
        this.columns = columns;

        WAND_INVENTORY_TYPES.register(this);
    }

    public Inventory newInventory(WandHolder wand) {
        return inventoryProducer.apply(wand);
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
