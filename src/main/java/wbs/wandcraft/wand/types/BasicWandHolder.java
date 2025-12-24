package wbs.wandcraft.wand.types;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.wand.WandHolder;

import java.util.List;

public final class BasicWandHolder extends WandHolder<BasicWand> {

    public static final int WAND_DISPLAY_SLOT = 1;
    public static final int ITEM_SLOT = 4;

    public BasicWandHolder(BasicWand wand, ItemStack item) {
        super(wand, item);
    }

    @Override
    public Integer getWandDisplaySlot() {
        return WAND_DISPLAY_SLOT;
    }
    @Override
    public Integer getUpgradeDisplaySlot() {
        return null;
    }
    @Override
    public List<Integer> getUpgradeSlots() {
        return List.of();
    }

    @Override
    protected @NotNull ItemStack getFakeWand() {
        ItemStack fakeWand = super.getFakeWand();

        Style style = Style.style(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false);

        // TODO: Make this configurable
        fakeWand.lore(List.of(
                Component.text("Place a spell scroll in the below slot!").style(style)
        ));

        return fakeWand;
    }

    @Override
    protected Inventory instantiateInventory() {
        return Bukkit.createInventory(this, InventoryType.DROPPER, wandItem.effectiveName().color(NamedTextColor.DARK_GRAY));
    }

    @Override
    protected void reload() {
        ItemStack item = wand.getItem();

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, MAIN_OUTLINE);
        }

        inventory.setItem(ITEM_SLOT, item);

        super.reload();
    }

    @Override
    public void saveItems() {
        wand.setItem(inventory.getItem(ITEM_SLOT));
    }

    @Override
    public boolean isItemSlot(int slot) {
        return slot == ITEM_SLOT;
    }
}
