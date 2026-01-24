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
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.WbsEventUtils;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.modifier.SpellModifier;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class WandHolder<T extends Wand> implements InventoryHolder {
    protected static int slot(int row, int column) {
        return row * 9 + column;
    }
    protected static final ItemStack MAIN_OUTLINE = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
    protected static final ItemStack SECONDARY_OUTLINE = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    protected static final ItemStack UPGRADE_DISPLAY = new ItemStack(Material.STRUCTURE_VOID);
    protected static final ItemStack LOCKED_SLOT = new ItemStack(Material.STRUCTURE_VOID);
    protected static final ItemStack SLOT_LABEL = new ItemStack(Material.PURPLE_BANNER);

    public static final int FULL_INV_COLUMNS = 9;

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

        inventory = instantiateInventory();
        reload();
    }

    protected void reload() {
        populateBonusSlots();
        populateUpgrades();
    }

    private void populateUpgrades() {
        int upgradeIndex = 0;
        List<ItemStack> upgrades = wand.getUpgrades();

        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = row * 9 + column;

                if (isUpgradeSlot(slot)) {
                    if (upgrades.size() > upgradeIndex) {
                        ItemStack upgradeItem = upgrades.get(upgradeIndex);

                        inventory.setItem(slot, upgradeItem);

                        upgradeIndex++;
                    } else {
                        inventory.setItem(slot, null);
                    }
                }
            }
        }
    }

    private void populateBonusSlots() {
        ItemStack fakeWand = getFakeWand();

        Integer wandDisplaySlot = getWandDisplaySlot();
        if (wandDisplaySlot != null) {
            inventory.setItem(wandDisplaySlot, fakeWand);
        }
        Integer upgradeDisplaySlot = getUpgradeDisplaySlot();
        if (upgradeDisplaySlot != null) {
            inventory.setItem(upgradeDisplaySlot, UPGRADE_DISPLAY);
        }
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
        Integer wandDisplaySlot = getWandDisplaySlot();
        if (wandDisplaySlot != null && slot == wandDisplaySlot) {
            return true;
        }
        if (itemInSlot == null) {
            return false;
        }

        return MAIN_OUTLINE.isSimilar(itemInSlot)
                || SECONDARY_OUTLINE.isSimilar(itemInSlot)
                || UPGRADE_DISPLAY.isSimilar(itemInSlot)
                || LOCKED_SLOT.isSimilar(itemInSlot)
                || SLOT_LABEL.matchesWithoutData(itemInSlot, Set.of(DataComponentTypes.ITEM_NAME))
                ;
    }

    public abstract @Nullable Integer getWandDisplaySlot();

    public abstract @Nullable Integer getUpgradeDisplaySlot();

    public abstract List<Integer> getUpgradeSlots();
    public boolean isUpgradeSlot(int slot) {
        return getUpgradeSlots().contains(slot);
    }

    protected abstract Inventory instantiateInventory();

    public void save() {
        saveItems();
        saveUpgrades();
        wand.toItem(wandItem);
    }

    protected void saveUpgrades() {
        List<ItemStack> newUpgrades = new LinkedList<>();
        for (Integer upgradeSlot : getUpgradeSlots().stream().sorted().toList()) {
            ItemStack item = inventory.getItem(upgradeSlot);
            if (item != null && canContainUpgrade(item)) {
                newUpgrades.add(item);
            } else {
                newUpgrades.add(null);
            }
        }

        wand.setUpgrades(newUpgrades);
    }

    @Override
    public final @NotNull Inventory getInventory() {
        return inventory;
    }

    public final void handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        if (event.getView().getBottomInventory().equals(event.getClickedInventory())) {
            if (event.isShiftClick()) {
                slot = event.getView().getTopInventory().firstEmpty();
            } else {
                return;
            }
        }

        if (slot == -1) {
            return;
        }

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

                // Can't shift click into the current slot, but we might be able to redirect
                if (canContainUpgrade(addedItem) && event.isShiftClick()) {
                    Inventory playerInventory = event.getView().getBottomInventory();
                    if (playerInventory.equals(event.getClickedInventory())) {
                        for (int upgradeSlot : getUpgradeSlots()) {
                            Inventory wandInventory = event.getView().getTopInventory();
                            ItemStack existingItem = wandInventory.getItem(upgradeSlot);
                            if (existingItem == null || existingItem.isEmpty()) {
                                int clickedSlot = event.getSlot();
                                playerInventory.setItem(clickedSlot, ItemStack.empty());
                                wandInventory.setItem(upgradeSlot, addedItem);
                                save();
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void handleUpgradeClick(InventoryClickEvent event) {
        ItemStack addedItem = WbsEventUtils.getItemAddedToTopInventory(event);
        if (addedItem != null) {
            if (!canContainUpgrade(addedItem)) {
                event.setCancelled(true);

                // Can't shift click into the current slot, but we might be able to redirect
                if (canContainItem(addedItem) && event.isShiftClick()) {
                    Inventory playerInventory = event.getView().getBottomInventory();
                    if (playerInventory.equals(event.getClickedInventory())) {
                        for (int itemSlot : getItemSlots()) {
                            Inventory wandInventory = event.getView().getTopInventory();
                            ItemStack existingItem = wandInventory.getItem(itemSlot);
                            if (existingItem == null || existingItem.isEmpty()) {
                                int clickedSlot = event.getSlot();
                                playerInventory.setItem(clickedSlot, ItemStack.empty());
                                wandInventory.setItem(itemSlot, addedItem);
                                save();
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    protected void handleMenuClick(InventoryClickEvent event) {

    }

    public abstract void saveItems();

    public abstract boolean isItemSlot(int slot);
    private List<Integer> getItemSlots() {
        // TODO: Cache this
        List<Integer> itemSlots = new LinkedList<>();
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            if (isItemSlot(slot)) {
                itemSlots.add(slot);
            }
        }

        return itemSlots;
    }

    public boolean canContainItem(@NotNull ItemStack addedItem) {
        return SpellInstance.fromItem(addedItem) != null;
    }

    public boolean canContainUpgrade(@NotNull ItemStack addedItem) {
        return SpellModifier.fromItem(addedItem) != null;
    }

    public void saveNextTick(InventoryClickEvent event) {
        WbsWandcraft.getInstance().runSync(() -> {
            // Save if still open -- if not, it's been done in the Close event
            if (event.getWhoClicked().getOpenInventory().getTopInventory().getHolder() instanceof WandHolder<?> updatedHolder) {
                updatedHolder.save();
            }
        });
    }
}
