package wbs.wandcraft.equipment;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;

import java.util.HashMap;
import java.util.Map;

@NullMarked
public class EquipmentManager {
    public static final NamespacedKey MAGIC_EQUIPMENT_TYPE_KEY = WbsWandcraft.getKey("magic_equipment_type");

    @Nullable
    public static MagicEquipmentType getType(@Nullable ItemStack item) {
        if (item == null) {
            return null;
        }
        NamespacedKey typeKey = item.getPersistentDataContainer().get(MAGIC_EQUIPMENT_TYPE_KEY, WbsPersistentDataType.NAMESPACED_KEY);
        if (typeKey == null) {
            return null;
        }

        return WandcraftRegistries.MAGIC_EQUIPMENT_TYPES.get(typeKey);
    }

    public static Map<MagicEquipmentSlot, MagicEquipmentType> getMagicEquipment(LivingEntity livingEntity) {
        Map<MagicEquipmentSlot, MagicEquipmentType> magicEquipment = new HashMap<>();

        for (MagicEquipmentSlot slot : MagicEquipmentSlot.values()) {
            ItemStack itemInSlot = getItemInSlot(livingEntity, slot);

            if (itemInSlot != null && !itemInSlot.isEmpty()) {
                MagicEquipmentType type = getType(itemInSlot);

                if (type != null) {
                    magicEquipment.put(slot, type);
                }
            }
        }

        return magicEquipment;
    }

    @Nullable
    public static ItemStack getItemInSlot(LivingEntity entity, MagicEquipmentSlot slot) {
        EquipmentSlot bukkitSlot = slot.getBukkitSlot();
        if (bukkitSlot != null) {
            EntityEquipment equipment = entity.getEquipment();
            if (equipment == null) {
                return null;
            }

            if (entity.canUseEquipmentSlot(bukkitSlot)) {
                return equipment.getItem(bukkitSlot);
            } else {
                return null;
            }
        } else {
            // TODO: Implement non-vanilla slots
            return null;
        }
    }
}
