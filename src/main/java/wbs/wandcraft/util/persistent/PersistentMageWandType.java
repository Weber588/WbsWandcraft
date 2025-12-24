package wbs.wandcraft.util.persistent;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.wand.types.MageWand;

import java.util.List;

public class PersistentMageWandType extends AbstractPersistentWandType<MageWand> {
    private static final NamespacedKey INVENTORY = WbsWandcraft.getKey("inventory");
    private static final NamespacedKey CURRENT_SLOT = WbsWandcraft.getKey("current_slot");

    @Override
    public @NotNull Class<MageWand> getComplexType() {
        return MageWand.class;
    }

    @Override
    protected void writeTo(PersistentDataContainer container, MageWand wand, @NotNull PersistentDataAdapterContext context) {
        container.set(INVENTORY, PersistentDataType.LIST.listTypeFrom(WbsPersistentDataType.ITEM_AS_BYTES), wand.getItems());
        container.set(CURRENT_SLOT, PersistentDataType.INTEGER, wand.getCurrentSlot());
    }

    @Override
    protected @NotNull MageWand getWand(@NotNull PersistentDataContainer container, @NotNull String uuid) {
        return new MageWand(uuid);
    }

    @Override
    protected void populateWand(MageWand wand, @NotNull PersistentDataContainer container) {
        List<ItemStack> items = container.get(INVENTORY, PersistentDataType.LIST.listTypeFrom(WbsPersistentDataType.ITEM_AS_BYTES));

        if (items != null) {
            wand.setItems(items);
        }

        Integer currentSlot = container.get(CURRENT_SLOT, PersistentDataType.INTEGER);
        if (currentSlot != null) {
            wand.setCurrentSlot(currentSlot);
        }
    }
}
