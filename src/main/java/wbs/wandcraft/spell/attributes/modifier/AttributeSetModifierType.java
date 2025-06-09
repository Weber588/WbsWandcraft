package wbs.wandcraft.spell.attributes.modifier;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.RegisteredPersistentDataType;
import wbs.wandcraft.WbsWandcraft;

public class AttributeSetModifierType implements AttributeModifierType {
    @Override
    public @NotNull NamespacedKey getKey() {
        return WbsWandcraft.getKey("set");
    }

    @Override
    public <T, M> AttributeModificationOperator<T, M> buildModifierType(PersistentDataType<?, T> baseType, RegisteredPersistentDataType<M> modifierType) {
        if (!baseType.getComplexType().isAssignableFrom(modifierType.dataType().getComplexType())) {
            throw new IllegalArgumentException("Set only supports symmetric types");
        }

        //noinspection unchecked
        return (AttributeModificationOperator<T, M>) new AttributeSetOperator<>(this, modifierType);
    }
}
