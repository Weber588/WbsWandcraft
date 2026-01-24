package wbs.wandcraft.util.persistent;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.wand.types.BarbarianWand;

public class PersistentBarbarianWandType extends AbstractPersistentWandType<BarbarianWand> {
    private static final NamespacedKey ITEM = WbsWandcraft.getKey("item");

    @Override
    public @NotNull Class<BarbarianWand> getComplexType() {
        return BarbarianWand.class;
    }

    @Override
    protected void writeTo(PersistentDataContainer container, BarbarianWand wand, @NotNull PersistentDataAdapterContext context) {
        if (wand.getItem() != null) {
            container.set(ITEM, WbsPersistentDataType.ITEM_AS_BYTES, wand.getItem());
        }
    }

    @Override
    protected @NotNull BarbarianWand getWand(@NotNull PersistentDataContainer container, @NotNull String uuid) {
        return new BarbarianWand(uuid);
    }

    @Override
    protected void populateWand(BarbarianWand wand, @NotNull PersistentDataContainer container) {
        ItemStack item = container.get(ITEM, WbsPersistentDataType.ITEM_AS_BYTES);

        wand.setItem(item);
    }
}
