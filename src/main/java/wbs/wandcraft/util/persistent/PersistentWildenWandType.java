package wbs.wandcraft.util.persistent;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.wand.types.WildenWand;

import java.util.List;

public class PersistentWildenWandType extends AbstractPersistentWandType<WildenWand> {
    private static final NamespacedKey INVENTORY = WbsWandcraft.getKey("inventory");
    private static final NamespacedKey COOLDOWN_TICKS = WbsWandcraft.getKey("cooldown_ticks");

    @Override
    public @NotNull Class<WildenWand> getComplexType() {
        return WildenWand.class;
    }

    @Override
    protected void writeTo(PersistentDataContainer container, WildenWand wand, @NotNull PersistentDataAdapterContext context) {
        container.set(INVENTORY, PersistentDataType.LIST.listTypeFrom(WbsPersistentDataType.ITEM_AS_BYTES), wand.getItems());
        container.set(COOLDOWN_TICKS, PersistentDataType.INTEGER, wand.getLastSpellCooldownTicks());
    }

    @Override
    protected @NotNull WildenWand getWand(@NotNull PersistentDataContainer container, @NotNull String uuid) {
        return new WildenWand(uuid);
    }

    @Override
    protected void populateWand(WildenWand wand, @NotNull PersistentDataContainer container) {
        List<ItemStack> items = container.get(INVENTORY, PersistentDataType.LIST.listTypeFrom(WbsPersistentDataType.ITEM_AS_BYTES));

        if (items != null) {
            wand.setItems(items);
        }

        wand.setLastSpellCooldownTicks(container.getOrDefault(COOLDOWN_TICKS, PersistentDataType.INTEGER, 0));
    }
}
