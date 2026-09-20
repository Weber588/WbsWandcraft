package wbs.wandcraft.spell.attributes.modifier;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.AttributeDataType;
import wbs.wandcraft.WbsWandcraft;

public class AttributeMultiplyModifierType implements AttributeModifierType {
    private final int priority;

    public AttributeMultiplyModifierType(int priority) {
        this.priority = priority;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return WbsWandcraft.getKey("multiply");
    }

    @Override
    public <T, M> AttributeModificationOperator<T, M> buildModifierType(PersistentDataType<?, T> baseType, AttributeDataType<M> modifierType) {
        if (!Number.class.isAssignableFrom(baseType.getComplexType())) {
            throw new IllegalArgumentException("Multiply only supports numeric types");
        }
        if (!Number.class.isAssignableFrom(modifierType.dataType().getComplexType())) {
            throw new IllegalArgumentException("Multiply only supports numeric types");
        }

        //noinspection unchecked
        return (AttributeModificationOperator<T, M>) new AttributeMultiplyOperator<>(
                this,
                (PersistentDataType<?, ? extends Number>) baseType,
                (AttributeDataType<? extends Number>) modifierType
        );
    }

    @Override
    public int priority() {
        return priority;
    }
}
