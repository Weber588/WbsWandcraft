package wbs.wandcraft.wand;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEventUtils;

import java.util.Set;

public abstract class WandHolder<T extends Wand> implements InventoryHolder {
    protected static final ItemStack MAIN_OUTLINE = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
    protected static final ItemStack SECONDARY_OUTLINE = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    private static final ItemStack UPGRADE_DISPLAY = new ItemStack(Material.STRUCTURE_VOID);

    static {
        MAIN_OUTLINE.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build());
        SECONDARY_OUTLINE.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build());
        UPGRADE_DISPLAY.setData(DataComponentTypes.ITEM_MODEL, Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE.getKey());
        UPGRADE_DISPLAY.setData(DataComponentTypes.ITEM_NAME, Component.text("Upgrades"));
    }

    protected final T wand;
    protected final ItemStack wandItem;
    protected final Inventory inventory;

    public WandHolder(T wand, ItemStack wandItem) {
        this.wand = wand;
        this.wandItem = wandItem;

        inventory = buildInventory();
        populateBonusSlots();
    }

    private void populateBonusSlots() {
        ItemStack fakeWand = getFakeWand();

        inventory.setItem(getWandDisplaySlot(), fakeWand);

        getUpgradeSlots().forEach(slot -> inventory.setItem(slot, null));

        inventory.setItem(getUpgradeDisplaySlot(), UPGRADE_DISPLAY);
    }

    protected @NotNull ItemStack getFakeWand() {
        ItemStack fakeWand = wandItem.clone();
        fakeWand.editPersistentDataContainer(container ->
                container.getKeys().forEach(container::remove)
        );
        fakeWand.resetData(DataComponentTypes.LORE);
        return fakeWand;
    }

    public abstract int getWandDisplaySlot();

    public abstract int getUpgradeDisplaySlot();

    public abstract Set<Integer> getUpgradeSlots();

    protected abstract Inventory buildInventory();

    public void save() {
        wand.updateItems(inventory);
        wand.toItem(wandItem);
    }

    public final T wand() {
        return wand;
    }

    public final ItemStack wandItem() {
        return wandItem;
    }

    @Override
    public final @NotNull Inventory getInventory() {
        return inventory;
    }

    public final boolean isItemSlot(int slot) {
        return wand.isItemSlot(slot);
    }

    public final void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        if (isItemSlot(slot)) {
            handleItemClick(event);
        } else {
            ItemStack itemInSlot = event.getCurrentItem();
            if (isDisplaySlot(slot, itemInSlot)) {
                event.setCancelled(true);
                return;
            }

            handleMenuClick(event);
        }
    }

    protected boolean isDisplaySlot(int slot, ItemStack itemInSlot) {
        return slot == getWandDisplaySlot() || MAIN_OUTLINE.isSimilar(itemInSlot) || SECONDARY_OUTLINE.isSimilar(itemInSlot) || UPGRADE_DISPLAY.isSimilar(itemInSlot);
    }

    protected abstract void handleMenuClick(InventoryClickEvent event);

    private void handleItemClick(InventoryClickEvent event) {
        ItemStack addedItem = WbsEventUtils.getItemAddedToTopInventory(event);
        if (addedItem != null) {
            if (!wand().canContain(addedItem)) {
                event.setCancelled(true);
            }
        }
    }
}
