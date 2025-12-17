package wbs.wandcraft.wand.types;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.spell.modifier.SpellModifier;
import wbs.wandcraft.wand.WandHolder;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public final class WizardryWandHolder extends WandHolder<WizardryWand> {
    private static final ItemStack LOCKED_SLOT = new ItemStack(Material.STRUCTURE_VOID);
    public static final int ITEM_COLUMN_START = 3;
    public static final int ITEM_COLUMN_END = 7;
    public static final int ITEM_ROW_END = 4;
    public static final int ITEM_ROW_START = 1;
    public static final int FULL_INV_COLUMNS = 9;
    public static final int FULL_INV_ROWS = 6;

    static {
        LOCKED_SLOT.setData(DataComponentTypes.ITEM_MODEL, Material.BARRIER.getKey());
        LOCKED_SLOT.setData(DataComponentTypes.ITEM_NAME, Component.text("Locked").color(NamedTextColor.RED));
    }

    private static int slot(int row, int column) {
        return row * 9 + column;
    }

    private static final int WAND_DISPLAY_SLOT = slot(0, 5);
    private static final int UPGRADE_DISPLAY_SLOT = slot(1, 1);
    private static final Set<Integer> UPGRADE_SLOTS = Set.of(
            slot(3, 1),
            slot(4, 1)
    );

    public WizardryWandHolder(WizardryWand wand, ItemStack item) {
        super(wand, item);
    }

    @Override
    public int getWandDisplaySlot() {
        return WAND_DISPLAY_SLOT;
    }
    @Override
    public int getUpgradeDisplaySlot() {
        return UPGRADE_DISPLAY_SLOT;
    }
    @Override
    public Set<Integer> getUpgradeSlots() {
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
    protected boolean isDisplaySlot(int slot, ItemStack itemInSlot) {
        return super.isDisplaySlot(slot, itemInSlot) || itemInSlot.isSimilar(LOCKED_SLOT);
    }

    @Override
    protected Inventory buildInventory() {
        Inventory inventory = Bukkit.createInventory(this, 6 * 9, wandItem.effectiveName().color(NamedTextColor.BLACK));

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
                    continue;
                }

                if (column == 0 || column == 2 || column == 8) {
                    inventory.setItem(slot, SECONDARY_OUTLINE);
                } else {
                    inventory.setItem(slot, MAIN_OUTLINE);
                }
            }
        }

        return inventory;
    }

    @Override
    protected void handleMenuClick(InventoryClickEvent event) {

    }

    @Override
    public void updateItems(Inventory inventory) {
        List<ItemStack> items = wand.getItems();
        List<ItemStack> newItems = new LinkedList<>();

        // TODO: Make upgrade slots save somewhere
        for (int column = ITEM_COLUMN_START; column < ITEM_COLUMN_END; column++) {
            for (int row = ITEM_ROW_START; row < ITEM_ROW_END; row++) {
                int slot = row * FULL_INV_COLUMNS + column;
                ItemStack item = inventory.getItem(slot);
                if (item == null || canContainItem(item)) {
                    newItems.add(item);
                } else {
                    newItems.add(null);
                }
            }
        }
        
        items.clear();
        items.addAll(newItems);
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

    @Override
    public boolean canContainItem(@NotNull ItemStack addedItem) {
        return super.canContainItem(addedItem) || SpellModifier.fromItem(addedItem) != null;
    }
}
