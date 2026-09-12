package wbs.wandcraft.spell.attributes.modifier;

import org.bukkit.Keyed;
import org.bukkit.persistence.PersistentDataType;
import wbs.wandcraft.AttributeDataType;

public interface AttributeModifierType extends Keyed {
    AttributeModifierType SET = new AttributeSetModifierType();
    AttributeModifierType MULTIPLY = new AttributeMultiplyModifierType();
    AttributeModifierType ADD = new AttributeAddModifierType();

    <T, M> AttributeModificationOperator<T, M> buildModifierType(
            PersistentDataType<?, T> baseType,
            AttributeDataType<M> modifierType
    );
}
