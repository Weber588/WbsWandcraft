package wbs.wandcraft.wand;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

public interface WandInventoryType {
    WandInventoryType LINE_5 = wandInv -> Bukkit.createInventory(wandInv, InventoryType.HOPPER, Component.text("Inventory"));
    WandInventoryType LINE_9 = wandInv -> Bukkit.createInventory(wandInv, 9, Component.text("Inventory"));
    WandInventoryType PLANE_3x3 = wandInv -> Bukkit.createInventory(wandInv, InventoryType.DISPENSER, Component.text("Inventory"));
    WandInventoryType PLANE_2x9 = wandInv -> Bukkit.createInventory(wandInv, 2 * 9, Component.text("Inventory"));

    Inventory newInventory(WandInventory wandInv);
}
