package wbs.wandcraft.equipment;

import org.bukkit.Keyed;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.wandcraft.util.ItemDecorator;
import wbs.wandcraft.util.ItemUtils;

import java.util.Map;

@NullMarked
public interface MagicEquipmentType extends ItemDecorator, Keyed {
    default void registerEvents() {}
    String getName();

    @Override
    default void toItem(ItemStack item) {
        ItemDecorator.super.toItem(item);

        item.editPersistentDataContainer(
                container -> {
                    container.set(EquipmentManager.MAGIC_EQUIPMENT_TYPE_KEY, WbsPersistentDataType.NAMESPACED_KEY, getKey());
                    container.set(ItemUtils.WANDCRAFT_ITEM_NAME, PersistentDataType.STRING, getName());
                    container.set(ItemUtils.WANDCRAFT_ITEM_KEY, WbsPersistentDataType.NAMESPACED_KEY, getKey());
                }
        );
    }

    default void ifEquipped(LivingEntity livingEntity, Runnable ifEquipped) {
        Map<MagicEquipmentSlot, MagicEquipmentType> magicEquipment = EquipmentManager.getMagicEquipment(livingEntity);
        for (MagicEquipmentType magicEquipmentType : magicEquipment.values()) {
            if (magicEquipmentType == this) {
                ifEquipped.run();
            }
        }
    }
}
