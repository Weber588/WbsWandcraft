package wbs.wandcraft.equipment;

import org.bukkit.Keyed;
import org.bukkit.inventory.ItemStack;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.wandcraft.util.ItemDecorator;

public interface MagicEquipmentType extends ItemDecorator, Keyed {
    default void registerEvents() {}

    @Override
    default void toItem(ItemStack item) {
        ItemDecorator.super.toItem(item);

        item.editPersistentDataContainer(
                container -> {
                    container.set(EquipmentManager.MAGIC_EQUIPMENT_TYPE_KEY, WbsPersistentDataType.NAMESPACED_KEY, getKey());
                }
        );
    }
}
