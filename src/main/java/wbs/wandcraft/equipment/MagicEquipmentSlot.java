package wbs.wandcraft.equipment;

import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.Nullable;

public enum MagicEquipmentSlot {
    HEAD(EquipmentSlot.HEAD),
    CHEST(EquipmentSlot.CHEST),
    LEGS(EquipmentSlot.LEGS),
    FEET(EquipmentSlot.FEET),
    MAIN_HAND(EquipmentSlot.HAND),
    OFF_HAND(EquipmentSlot.OFF_HAND),
    TRINKET_NECKLACE, // TODO: Implement trinket slots
    ;

    @Nullable
    private final EquipmentSlot bukkitSlot;

    MagicEquipmentSlot() {
        this(null);
    }
    MagicEquipmentSlot(@Nullable EquipmentSlot bukkitSlot) {
        this.bukkitSlot = bukkitSlot;
    }

    public @Nullable EquipmentSlot getBukkitSlot() {
        return bukkitSlot;
    }
}
