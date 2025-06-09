package wbs.wandcraft.spell.attributes.modifier;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.RegisteredPersistentDataType;
import wbs.wandcraft.WbsWandcraft;

public class AttributeAddModifierType implements AttributeModifierType {
    @Override
    public @NotNull NamespacedKey getKey() {
        return WbsWandcraft.getKey("add");
    }

    @Override
    public <T, M> AttributeModificationOperator<T, M> buildModifierType(PersistentDataType<?, T> baseType, RegisteredPersistentDataType<M> modifierType) {
        if (!Number.class.isAssignableFrom(baseType.getComplexType())) {
            throw new IllegalArgumentException("Add only supports numeric types");
        }
        if (!Number.class.isAssignableFrom(modifierType.dataType().getComplexType())) {
            throw new IllegalArgumentException("Add only supports numeric types");
        }

        //noinspection unchecked
        return (AttributeModificationOperator<T, M>) new AttributeAddOperator<>(
                this,
                (PersistentDataType<?, ? extends Number>) baseType,
                (RegisteredPersistentDataType<? extends Number>) modifierType
        );
    }
}
