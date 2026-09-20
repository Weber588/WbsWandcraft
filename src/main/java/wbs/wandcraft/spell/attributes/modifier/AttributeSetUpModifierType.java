package wbs.wandcraft.spell.attributes.modifier;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.AttributeDataType;
import wbs.wandcraft.WbsWandcraft;

public class AttributeSetUpModifierType implements AttributeModifierType {
    private final int priority;

    public AttributeSetUpModifierType(int priority) {
        this.priority = priority;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return WbsWandcraft.getKey("set_up");
    }

    @Override
    public <T, M> AttributeModificationOperator<T, M> buildModifierType(PersistentDataType<?, T> baseType, AttributeDataType<M> modifierType) {
        if (!baseType.getComplexType().isAssignableFrom(modifierType.dataType().getComplexType())) {
            throw new IllegalArgumentException("Set only supports symmetric types");
        }

        //noinspection unchecked
        return (AttributeModificationOperator<T, M>) new AttributeSetUpOperator<>(this, modifierType);
    }

    @Override
    public int priority() {
        return priority;
    }
}
