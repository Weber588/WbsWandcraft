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
import wbs.utils.util.string.WbsStrings;
import wbs.wandcraft.wand.WandHolder;

import java.util.LinkedList;
import java.util.List;

public final class BroomstickWandHolder extends WandHolder<BroomstickWand> {
    public static final int WAND_DISPLAY_SLOT = 1;

    public BroomstickWandHolder(BroomstickWand wand, ItemStack item) {
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
        String description = "This wand will cast the last spell you cast - no spell slots!";

        List<Component> lore = new LinkedList<>(WbsStrings.wrapText(description, 141).stream()
                .map(Component::text)
                .map(component -> component.style(style))
                .toList());

        fakeWand.lore(lore);

        return fakeWand;
    }

    @Override
    protected Inventory instantiateInventory() {
        return Bukkit.createInventory(this, InventoryType.DROPPER, wandItem.effectiveName().color(NamedTextColor.DARK_GRAY));
    }

    @Override
    public void saveItems() {

    }

    @Override
    public boolean isItemSlot(int slot) {
        return false;
    }
}
