package wbs.wandcraft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.format.TextDecorationAndState;
import net.kyori.adventure.util.TriState;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ItemDecorator {
    // Probably don't need to, but making this static since it should never be overridden -- implementations shouldn't
    // have access to the item meta as they could change other things, even if it's default only those two in a default
    // method.
    static void decorate(ItemDecorator decorator, ItemMeta meta) {
        Component itemName = decorator.getItemName();
        if (itemName != null) {
            meta.itemName(itemName);
        }
        TextDecorationAndState notItalic = TextDecoration.ITALIC.withState(TriState.FALSE);
        meta.lore(
                decorator.getLore().stream()
                        .map(component ->
                            component.applyFallbackStyle(Style.style(notItalic))
                        )
                        .toList()
        );
    }

    @Nullable
    default Component getItemName() {
        return null;
    }

    @NotNull
    List<Component> getLore();
}
