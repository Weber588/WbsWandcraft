package wbs.wandcraft.wand.types;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.string.WbsStrings;
import wbs.wandcraft.wand.WandHolder;

import java.util.LinkedList;
import java.util.List;

public final class ApprenticeWandHolder extends WandHolder<ApprenticeWand> {
    public static final int WAND_DISPLAY_SLOT = 4;
    public static final int LEFT_SLOT = 12;
    public static final int RIGHT_SLOT = 14;

    public ApprenticeWandHolder(ApprenticeWand wand, ItemStack item) {
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
        String description = "Place a spell scroll in the below slots!";

        List<Component> lore = new LinkedList<>(WbsStrings.wrapText(description, 141).stream()
                .map(Component::text)
                .map(component -> component.style(style))
                .toList());

        fakeWand.lore(lore);

        return fakeWand;
    }

    @Override
    protected Inventory instantiateInventory() {
        return Bukkit.createInventory(this, 3 * FULL_INV_COLUMNS, getInventoryName());
    }

    @Override
    protected void reload() {
        ItemStack left = wand.getLeft();
        ItemStack right = wand.getRight();

        for (int slot = 0; slot < inventory.getSize(); slot++) {
            int outerBanners = 2;
            if (slot % FULL_INV_COLUMNS < outerBanners || slot % FULL_INV_COLUMNS > FULL_INV_COLUMNS - outerBanners - 1) {
                inventory.setItem(slot, SECONDARY_OUTLINE);
            } else {
                inventory.setItem(slot, MAIN_OUTLINE);
            }
        }

        inventory.setItem(LEFT_SLOT, left);
        inventory.setItem(RIGHT_SLOT, right);

        super.reload();
    }

    @Override
    public void saveItems() {
        wand.setLeft(inventory.getItem(LEFT_SLOT));
        wand.setRight(inventory.getItem(RIGHT_SLOT));
    }

    @Override
    public boolean isItemSlot(int slot) {
        return slot == LEFT_SLOT || slot == RIGHT_SLOT;
    }
}
