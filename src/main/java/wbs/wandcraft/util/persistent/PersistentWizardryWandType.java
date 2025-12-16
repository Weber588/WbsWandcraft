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

public class PersistentWizardryWandType extends AbstractPersistentWandType<WizardryWand> {
    private static final NamespacedKey INVENTORY = WbsWandcraft.getKey("inventory");

    @Override
    public @NotNull Class<WizardryWand> getComplexType() {
        return WizardryWand.class;
    }

    @Override
    protected void writeTo(PersistentDataContainer container, WizardryWand wand, @NotNull PersistentDataAdapterContext context) {
        PersistentDataContainer itemsContainer = context.newPersistentDataContainer();
        wand.getItems().rowMap().forEach((row, columnMap) -> {
            PersistentDataContainer columnContainer = context.newPersistentDataContainer();

            columnMap.forEach((column, item) -> {
                columnContainer.set(WbsWandcraft.getKey(column.toString()), WbsPersistentDataType.ITEM_AS_BYTES, item);
            });

            itemsContainer.set(WbsWandcraft.getKey(row.toString()), PersistentDataType.TAG_CONTAINER, columnContainer);
        });

        container.set(INVENTORY, PersistentDataType.TAG_CONTAINER, itemsContainer);
    }

    @Override
    protected @NotNull WizardryWand getWand(@NotNull PersistentDataContainer container, @NotNull String uuid) {
        return new WizardryWand(uuid);
    }

    @Override
    protected void populateWand(WizardryWand wand, @NotNull PersistentDataContainer container) {
        PersistentDataContainer itemsContainer = container.get(INVENTORY, PersistentDataType.TAG_CONTAINER);
        if (itemsContainer != null) {
            for (NamespacedKey rowKey : itemsContainer.getKeys()) {
                PersistentDataContainer rowContainer = itemsContainer.get(rowKey, PersistentDataType.TAG_CONTAINER);

                if (rowContainer != null) {
                    for (NamespacedKey columnKey : rowContainer.getKeys()) {
                        ItemStack item = rowContainer.get(columnKey, WbsPersistentDataType.ITEM_AS_BYTES);

                        if (item != null) {
                            Integer row = Integer.valueOf(rowKey.value());
                            Integer column = Integer.valueOf(columnKey.value());

                            wand.getItems().put(row, column, item);
                        }
                    }
                }
            }
        }
    }
}
