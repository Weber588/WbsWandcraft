package wbs.wandcraft.spell.attributes.modifier;

import org.bukkit.Keyed;
import org.bukkit.persistence.PersistentDataType;
import wbs.wandcraft.AttributeDataType;

public interface AttributeModifierType extends Keyed {
    // Set first; essentially overrides the default
    AttributeModifierType SET_UP = new AttributeSetUpModifierType(0);
    AttributeModifierType SET = new AttributeSetModifierType(1);
    AttributeModifierType ADD = new AttributeAddModifierType(2);
    AttributeModifierType MULTIPLY = new AttributeMultiplyModifierType(3);

    <T, M> AttributeModificationOperator<T, M> buildModifierType(
            PersistentDataType<?, T> baseType,
            AttributeDataType<M> modifierType
    );

    int priority();
}
