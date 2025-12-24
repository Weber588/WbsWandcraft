package wbs.wandcraft.wand.types;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.wand.WandHolder;

import java.util.LinkedList;
import java.util.List;

public final class MageWandHolder extends WandHolder<MageWand> {

    private static int slot(int row, int column) {
        return row * 9 + column;
    }

    private static final int WAND_DISPLAY_SLOT = slot(2, 4);
    private static final int UPGRADE_DISPLAY_SLOT = slot(0, 4);
    private static final List<Integer> UPGRADE_SLOTS = List.of(
            slot(1, 2),
            slot(1, 3),
            slot(1, 4),
            slot(1, 5),
            slot(1, 6)
    );
    private static final int ITEM_COLUMN_START = 1;
    private static final int ITEM_COLUMN_END = 7;
    private static final int ITEM_ROW_START = 3;
    private static final int ITEM_ROW_END = 4;

    public MageWandHolder(MageWand wand, ItemStack item) {
        super(wand, item);
    }

    @Override
    public Integer getWandDisplaySlot() {
        return WAND_DISPLAY_SLOT;
    }
    @Override
    public Integer getUpgradeDisplaySlot() {
        return UPGRADE_DISPLAY_SLOT;
    }
    @Override
    public List<Integer> getUpgradeSlots() {
        return UPGRADE_SLOTS;
    }

    @Override
    protected @NotNull ItemStack getFakeWand() {
        ItemStack fakeWand = super.getFakeWand();

        Style style = Style.style(NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false);
        Style keybindStyle = Style.style(NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false);

        // TODO: Make this configurable
        fakeWand.lore(List.of(
                Component.text("Place spell scrolls in the below slots!").style(style),
                Component.text("You can cycle between spells to cast with").style(style),
                Component.keybind("key.drop").style(keybindStyle).append(Component.text(" and ")).append(Component.keybind("key.sneak").style(keybindStyle)).append(Component.text("+").style(style)).append(Component.keybind("key.drop").style(keybindStyle))
        ));

        return fakeWand;
    }

    @Override
    protected Inventory instantiateInventory() {
        return Bukkit.createInventory(this, 6 * 9, wandItem.effectiveName().color(NamedTextColor.DARK_GRAY));
    }

    @Override
    protected void reload() {
        List<ItemStack> items = wand.getItems();
        int itemIndex = 0;
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = row * 9 + column;
                if (isItemSlot(slot)) {
                    if (items.size() > itemIndex) {
                        inventory.setItem(slot, items.get(itemIndex));
                    }

                    itemIndex++;
                } else if (row == 0 || row == 2 || row == 5) {
                    inventory.setItem(slot, SECONDARY_OUTLINE);
                } else {
                    inventory.setItem(slot, MAIN_OUTLINE);
                }
            }
        }

        super.reload();
    }

    @Override
    public void saveItems() {
        List<ItemStack> newItems = new LinkedList<>();

        for (int column = ITEM_COLUMN_START; column <= ITEM_COLUMN_END; column++) {
            for (int row = ITEM_ROW_START; row <= ITEM_ROW_END; row++) {
                int slot = row * FULL_INV_COLUMNS + column;
                ItemStack item = inventory.getItem(slot);
                if (item == null || canContainItem(item)) {
                    newItems.add(item);
                } else {
                    newItems.add(null);
                }
            }
        }

        wand.setItems(newItems);
    }

    @Override
    public boolean isItemSlot(int slot) {
        int row = slot / FULL_INV_COLUMNS;
        int column = slot % FULL_INV_COLUMNS;

        return row >= ITEM_ROW_START &&
                row <= ITEM_ROW_END &&
                column >= ITEM_COLUMN_START &&
                column <= ITEM_COLUMN_END
                ;
    }
}
