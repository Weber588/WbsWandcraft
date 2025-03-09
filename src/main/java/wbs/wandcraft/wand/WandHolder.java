package wbs.wandcraft.wand;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.util.CustomPersistentDataTypes;

import java.util.Objects;

public final class WandHolder implements InventoryHolder {
    private final Wand wand;
    private final ItemStack item;
    private final Inventory inventory;

    public WandHolder(Wand wand, ItemStack item) {
        this.wand = wand;
        this.item = item;
        inventory = wand.getInventory(this);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void save() {
        wand.updateItems(inventory);
        item.editMeta(meta ->
                meta.getPersistentDataContainer().set(Wand.WAND_KEY, CustomPersistentDataTypes.WAND, wand)
        );
    }

    public Wand wand() {
        return wand;
    }

    public ItemStack item() {
        return item;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (WandHolder) obj;
        return Objects.equals(this.wand, that.wand) &&
                Objects.equals(this.item, that.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(wand, item);
    }

    @Override
    public String toString() {
        return "WandHolder[" +
                "wand=" + wand + ", " +
                "item=" + item + ']';
    }

}
