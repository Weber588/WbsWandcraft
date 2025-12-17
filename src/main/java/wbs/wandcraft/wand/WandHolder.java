package wbs.wandcraft.wand;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEventUtils;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.modifier.SpellModifier;

import java.util.Set;

public abstract class WandHolder<T extends Wand> implements InventoryHolder {
    protected static final ItemStack MAIN_OUTLINE = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
    protected static final ItemStack SECONDARY_OUTLINE = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    protected static final ItemStack UPGRADE_DISPLAY = new ItemStack(Material.STRUCTURE_VOID);
    protected static final ItemStack LOCKED_SLOT = new ItemStack(Material.STRUCTURE_VOID);

    static {
        MAIN_OUTLINE.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build());
        SECONDARY_OUTLINE.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build());

        UPGRADE_DISPLAY.setData(DataComponentTypes.ITEM_MODEL, Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE.getKey());
        UPGRADE_DISPLAY.setData(DataComponentTypes.ITEM_NAME, Component.text("Upgrades"));

        LOCKED_SLOT.setData(DataComponentTypes.ITEM_MODEL, Material.BARRIER.getKey());
        LOCKED_SLOT.setData(DataComponentTypes.ITEM_NAME, Component.text("Locked").color(NamedTextColor.RED));
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

    protected boolean isDisplaySlot(int slot, ItemStack itemInSlot) {
        if (slot == getWandDisplaySlot()) {
            return true;
        }
        if (itemInSlot == null) {
            return false;
        }

        return MAIN_OUTLINE.isSimilar(itemInSlot)
                || SECONDARY_OUTLINE.isSimilar(itemInSlot)
                || UPGRADE_DISPLAY.isSimilar(itemInSlot)
                || LOCKED_SLOT.isSimilar(itemInSlot)
                ;
    }

    public abstract int getWandDisplaySlot();

    public abstract int getUpgradeDisplaySlot();

    public abstract Set<Integer> getUpgradeSlots();
    public boolean isUpgradeSlot(int slot) {
        return getUpgradeSlots().contains(slot);
    }

    protected abstract Inventory buildInventory();

    public void save() {
        updateItems(inventory);
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

    public final void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();

        ItemStack itemInSlot = event.getCurrentItem();

        if (isDisplaySlot(slot, itemInSlot)) {
            event.setCancelled(true);
            return;
        }

        if (isItemSlot(slot)) {
            handleItemClick(event);
        } else if (isUpgradeSlot(slot)){
            handleUpgradeClick(event);
        } else {
            handleMenuClick(event);
        }
    }

    private void handleItemClick(InventoryClickEvent event) {
        ItemStack addedItem = WbsEventUtils.getItemAddedToTopInventory(event);
        if (addedItem != null) {
            if (!canContainItem(addedItem)) {
                event.setCancelled(true);
            }
        }
    }

    private void handleUpgradeClick(InventoryClickEvent event) {
        ItemStack addedItem = WbsEventUtils.getItemAddedToTopInventory(event);
        if (addedItem != null) {
            if (!canContainUpgrade(addedItem)) {
                event.setCancelled(true);
            }
        }
    }

    protected void handleMenuClick(InventoryClickEvent event) {

    }

    public abstract void updateItems(Inventory inventory);

    public abstract boolean isItemSlot(int slot);

    public boolean canContainItem(@NotNull ItemStack addedItem) {
        return SpellInstance.fromItem(addedItem) != null;
    }

    public boolean canContainUpgrade(@NotNull ItemStack addedItem) {
        return SpellModifier.fromItem(addedItem) != null;
    }
}
