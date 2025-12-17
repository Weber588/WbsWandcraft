package wbs.wandcraft.util.persistent;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.wand.types.WizardryWand;

import java.util.List;

public class PersistentWizardryWandType extends AbstractPersistentWandType<WizardryWand> {
    private static final NamespacedKey INVENTORY = WbsWandcraft.getKey("inventory");

    @Override
    public @NotNull Class<WizardryWand> getComplexType() {
        return WizardryWand.class;
    }

    @Override
    protected void writeTo(PersistentDataContainer container, WizardryWand wand, @NotNull PersistentDataAdapterContext context) {
        container.set(INVENTORY, PersistentDataType.LIST.listTypeFrom(WbsPersistentDataType.ITEM_AS_BYTES), wand.getItems());
    }

    @Override
    protected @NotNull WizardryWand getWand(@NotNull PersistentDataContainer container, @NotNull String uuid) {
        return new WizardryWand(uuid);
    }

    @Override
    protected void populateWand(WizardryWand wand, @NotNull PersistentDataContainer container) {
        List<ItemStack> itemStacks = container.get(INVENTORY, PersistentDataType.LIST.listTypeFrom(WbsPersistentDataType.ITEM_AS_BYTES));

        if (itemStacks != null) {
            wand.getItems().addAll(itemStacks);
        }
    }
}
