package wbs.wandcraft;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ItemDecorator {
    // Probably don't need to, but making this static since it should never be overridden -- implementations shouldn't
    // have access to the item meta as they could change other things, even if it's default only those two in a default
    // method.
    static void decorate(ItemDecorator decorator, ItemMeta meta) {
        meta.itemName(decorator.getItemName());
        meta.lore(decorator.getLore());
    }

    @Nullable
    default Component getItemName() {
        return null;
    }

    @NotNull
    List<Component> getLore();
}
