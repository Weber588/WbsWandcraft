package wbs.wandcraft.spell.attributes.modifier;

import net.kyori.adventure.text.Component;
import wbs.wandcraft.RegisteredPersistentDataType;
import wbs.wandcraft.spell.attributes.SpellAttribute;

public class AttributeSetOperator<T> extends AttributeModificationOperator<T, T> {
    public AttributeSetOperator(AttributeModifierType definition, RegisteredPersistentDataType<T> baseType) {
        super(definition, baseType.dataType(), baseType);
    }

    @Override
    public T modify(T current, T value) {
        return value;
    }

    @Override
    public Component asComponent(SpellAttribute<T> attribute, T modifierValue) {
        return attribute.displayName().append(Component.text(" = " + attribute.formatValue(modifierValue)));
    }

    @Override
    public String toString() {
        return "=";
    }
}
