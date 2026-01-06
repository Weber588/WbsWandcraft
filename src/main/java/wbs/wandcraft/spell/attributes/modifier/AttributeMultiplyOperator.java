package wbs.wandcraft.spell.attributes.modifier;

import net.kyori.adventure.text.Component;
import org.bukkit.persistence.PersistentDataType;
import wbs.wandcraft.RegisteredPersistentDataType;
import wbs.wandcraft.spell.attributes.SpellAttribute;

import java.text.DecimalFormat;
import java.util.Objects;

public class AttributeMultiplyOperator<T extends Number, M extends Number> extends AttributeModificationOperator<T, M> {
    // TODO: Make this configurable. Not sure about it, since divide symbol looks a lot like plus in game
    private static final boolean DO_DIVISION_SYMBOL = false;

    public AttributeMultiplyOperator(AttributeModifierType definition, PersistentDataType<?, T> baseType, RegisteredPersistentDataType<M> modifierType) {
        super(definition, baseType, modifierType);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T modify(T current, M value) {
        if (Objects.requireNonNull(current) instanceof Integer integer) {
            return (T) Double.valueOf(Math.round(integer * value.doubleValue()));
        }

        return (T) Double.valueOf(current.doubleValue() * value.doubleValue());
    }

    @Override
    public Component asComponent(SpellAttribute<T> attribute, M modifierValue) {
        DecimalFormat format = new DecimalFormat("0.#");

        double asDouble = modifierValue.doubleValue();

        if (DO_DIVISION_SYMBOL) {
            double inverse = 1 / asDouble;
            if (inverse % 1 == 0) {
                return Component.text(" ÷" + format.format(inverse));
            }
        }

        if (asDouble < 2 && asDouble > 0) {
            double asPercentage = asDouble * 100;

            Component display;
            if (asPercentage < 100) {
                double displayPercent = 100 - asPercentage;

                display = Component.text(" -" + format.format(displayPercent) + "%");
            } else {
                double displayPercent = asPercentage - 100;

                display = Component.text(" +" + format.format(displayPercent) + "%");
            }

            return display;
        }

        return Component.text(" x" + format.format(modifierValue));
    }

    @Override
    public SpellAttribute.Polarity getPolarity(M modifierValue) {
        return modifierValue.doubleValue() < 1 ? SpellAttribute.Polarity.NEGATIVE : SpellAttribute.Polarity.POSITIVE;
    }

    @Override
    public String toString() {
        return "*";
    }
}
