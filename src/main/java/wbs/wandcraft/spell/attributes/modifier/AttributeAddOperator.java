package wbs.wandcraft.spell.attributes.modifier;

import net.kyori.adventure.text.Component;
import org.bukkit.persistence.PersistentDataType;
import wbs.wandcraft.RegisteredPersistentDataType;
import wbs.wandcraft.spell.attributes.SpellAttribute;

public class AttributeAddOperator<T extends Number, M extends Number> extends AttributeModificationOperator<T, M> {
    public AttributeAddOperator(AttributeModifierType definition, PersistentDataType<?, T> baseType, RegisteredPersistentDataType<M> modifierType) {
        super(definition, baseType, modifierType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T modify(T current, M value) {
        return (T) Double.valueOf(current.doubleValue() + value.doubleValue());
    }

    @Override
    public Component asComponent(SpellAttribute<T> attribute, M modifierValue) {
        return attribute.displayName().append(Component.text(" +" + attribute.formatAny(modifierValue)));
    }

    @Override
    public String toString() {
        return "+";
    }
}
