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
import wbs.wandcraft.wand.WandHolder;

import java.util.List;
import java.util.Set;

public final class WizardryWandHolder extends WandHolder<WizardryWand> {
    private static final ItemStack LOCKED_SLOT = new ItemStack(Material.STRUCTURE_VOID);

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

        int itemSlotsUnlocked = 0;
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = row * 9 + column;
                if (isItemSlot(slot)) {
                    if (itemSlotsUnlocked >= slots) {
                        inventory.setItem(slot, LOCKED_SLOT);
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

        wand.getItems().rowMap().forEach((row, map) -> {
            map.forEach((column, item) -> {
                inventory.setItem(row * 9 + column, item);
            });
        });

        return inventory;
    }

    @Override
    protected void handleMenuClick(InventoryClickEvent event) {

    }
}
