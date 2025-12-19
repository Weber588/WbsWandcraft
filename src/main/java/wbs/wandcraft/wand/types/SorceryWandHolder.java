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
import wbs.utils.util.WbsEnums;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.wand.WandHolder;

import java.util.*;

public final class SorceryWandHolder extends WandHolder<SorceryWand> {
    private static int slot(int row, int column) {
        return row * 9 + column;
    }

    private static final int PREV_TIER_SLOT = slot(0, 3);
    private static final int NEXT_TIER_SLOT = slot(0, 7);
    private static final ItemStack PREV_TIER_ITEM = new ItemStack(Material.RED_STAINED_GLASS_PANE);
    private static final ItemStack NEXT_TIER_ITEM = new ItemStack(Material.LIME_STAINED_GLASS_PANE);

    private static final int WAND_DISPLAY_SLOT = slot(0, 5);
    private static final int UPGRADE_DISPLAY_SLOT = slot(1, 1);
    private static final Set<Integer> UPGRADE_SLOTS = Set.of(
            slot(3, 1),
            slot(4, 1)
    );
    private static final Map<SorceryWand.WandControl, Integer> CONTROL_SLOTS = Map.of(
            SorceryWand.WandControl.SHIFT_DROP, slot(2, 5),
            SorceryWand.WandControl.PUNCH, slot(3, 3),
            SorceryWand.WandControl.RIGHT_CLICK, slot(3, 7),
            SorceryWand.WandControl.SHIFT_PUNCH, slot(5, 4),
            SorceryWand.WandControl.SHIFT_RIGHT_CLICK, slot(5, 6)
    );

    private int tier = 0;

    public SorceryWandHolder(SorceryWand wand, ItemStack item) {
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
                Component.text("Each one uses a different control, as").style(style),
                Component.text("labelled above.").style(style)
        ));

        return fakeWand;
    }

    @Override
    protected Inventory instantiateInventory() {
        return Bukkit.createInventory(this, 6 * 9, wandItem.effectiveName().color(NamedTextColor.DARK_GRAY));
    }

    @Override
    protected void reload() {
        Map<SorceryWand.WandControl, ItemStack> items = wand.getTieredItems(tier);

        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 9; column++) {
                int slot = row * 9 + column;

                if (CONTROL_SLOTS.containsValue(slot)) {
                    if (slot < 9) {
                        throw new IllegalStateException("Can't have sorcery slot in top row");
                    }
                    SorceryWand.WandControl control = CONTROL_SLOTS.keySet().stream()
                            .filter(check -> CONTROL_SLOTS.get(check) == slot)
                            .findAny()
                            .orElseThrow(IllegalStateException::new);

                    ItemStack slotLabel = SLOT_LABEL.clone();
                    slotLabel.setData(DataComponentTypes.ITEM_NAME, Component.text(WbsEnums.toPrettyString(control)));

                    inventory.setItem(slot - 9, slotLabel);

                    ItemStack item = items.get(control);

                    // Allow item to be nullable
                    inventory.setItem(slot, item);
                } else if (slot == PREV_TIER_SLOT && tier > 0) {
                    inventory.setItem(slot, PREV_TIER_ITEM);
                } else if (slot == NEXT_TIER_SLOT && tier < wand.getMaxTier()) {
                    inventory.setItem(slot, NEXT_TIER_ITEM);
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
        Map<SorceryWand.WandControl, ItemStack> newItems = new HashMap<>();

        CONTROL_SLOTS.forEach((control, slot) -> {
            ItemStack item = inventory.getItem(slot);
            if (item == null || canContainItem(item)) {
                newItems.put(control, item);
            } else {
                newItems.put(control, null);
            }
        });

        wand.setTierItems(tier, newItems);
    }

    @Override
    public boolean isItemSlot(int slot) {
        return CONTROL_SLOTS.containsValue(slot);
    }

    public void updateTier(int tier) {
        save();
        this.tier = tier;
        WbsWandcraft.getInstance().runLater(this::reload, 1);
    }

    @Override
    protected void handleMenuClick(InventoryClickEvent event) {
        super.handleMenuClick(event);

        if (!inventory.equals(event.getClickedInventory())) {
            return;
        }

        // Don't need to do checks on tier -- already done when these items were added to the menu
        if (PREV_TIER_ITEM.equals(event.getCurrentItem())) {
            event.setCancelled(true);
            updateTier(tier - 1);
        } else if (NEXT_TIER_ITEM.equals(event.getCurrentItem())) {
            event.setCancelled(true);
            updateTier(tier + 1);
        }
    }
}
