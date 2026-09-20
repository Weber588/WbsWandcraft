package wbs.wandcraft.spell.attributes.modifier;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import wbs.wandcraft.AttributeDataType;
import wbs.wandcraft.spell.attributes.SpellAttribute;

public class AttributeSetUpOperator<T> extends AttributeModificationOperator<T, T> {
    public AttributeSetUpOperator(AttributeModifierType definition, AttributeDataType<T> baseType) {
        super(definition, baseType.dataType(), baseType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T modify(T current, T value) {
        if (current instanceof Number currentNum && value instanceof Number valueNum) {
            return (T) (Double) Math.max(currentNum.doubleValue(), valueNum.doubleValue());
        }

        return value;
    }

    @Override
    public Component asComponent(SpellAttribute<T> attribute, T modifierValue) {
        return Component.text(" ∧ ").append(Component.text(attribute.formatValue(modifierValue)).color(NamedTextColor.AQUA));
    }

    @Override
    public SpellAttribute.Sentiment getSentiment(T modifierValue) {
        return SpellAttribute.Sentiment.NEUTRAL;
    }

    @Override
    public String toString() {
        return "∧";
    }
}
