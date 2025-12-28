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

public final class WizardryWandHolder extends WandHolder<WizardryWand> {

    private static int slot(int row, int column) {
        return row * 9 + column;
    }

    private static final int WAND_DISPLAY_SLOT = slot(0, 5);
    private static final int UPGRADE_DISPLAY_SLOT = slot(1, 1);
    private static final List<Integer> UPGRADE_SLOTS = List.of(
            slot(3, 1),
            slot(4, 1)
    );

    private static final int ITEM_COLUMN_START = 3;
    private static final int ITEM_COLUMN_END = 7;
    private static final int ITEM_ROW_END = 4;
    private static final int ITEM_ROW_START = 1;

    public WizardryWandHolder(WizardryWand wand, ItemStack item) {
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

        // TODO: Make this configurable
        fakeWand.lore(List.of(
                Component.text("Place spell scrolls in the below slots!").style(style),
                Component.text("When you use the wand, they'll ALL be cast").style(style),
                Component.text("in quick succession!").style(style)
        ));

        return fakeWand;
    }

    @Override
    protected Inventory instantiateInventory() {
        return Bukkit.createInventory(this, 6 * 9, wandItem.effectiveName().color(NamedTextColor.DARK_GRAY));
    }

    @Override
    protected void reload() {
        int slots = wand.getAttribute(WizardryWand.SLOTS, Integer.MAX_VALUE);

        List<ItemStack> items = wand.getItems();
        int itemIndex = 0;
        int itemSlotsUnlocked = 0;
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = row * 9 + column;
                if (isItemSlot(slot)) {
                    if (itemSlotsUnlocked >= slots) {
                        inventory.setItem(slot, LOCKED_SLOT);
                    } else {
                        if (items.size() > itemIndex) {
                            inventory.setItem(slot, items.get(itemIndex));
                        }

                        itemSlotsUnlocked++;
                        itemIndex++;
                    }
                } else if (column == 0 || column == 2 || column == 8) {
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

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (isItemSlot(slot)) {
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
