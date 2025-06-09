package wbs.wandcraft.generator;

import wbs.utils.util.WbsCollectionUtil;
import wbs.wandcraft.RegisteredPersistentDataType;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class AttributeModifierGenerator<T> {
    private final SpellAttribute<T> attribute;
    private final AttributeModifierType modifierType;
    private final List<ModifierValue<?>> values = new LinkedList<>();

    public AttributeModifierGenerator(SpellAttribute<T> attribute, AttributeModifierType modifierType) {
        this.attribute = attribute;
        this.modifierType = modifierType;
        values.add(new ModifierValue<>(attribute.type(), attribute.defaultValue()));
    }

    public SpellAttributeModifier<T, ?> get() {
        return WbsCollectionUtil.getRandom(values).createModifier(attribute, modifierType);
    }

    @SafeVarargs
    public final <M> AttributeModifierGenerator<T> setValues(RegisteredPersistentDataType<M> modifierType, M... values) {
        List<ModifierValue<M>> list = Arrays.stream(values)
                .map(value -> new ModifierValue<>(modifierType, value))
                .toList();
        return setValues(list);
    }

    public <M> AttributeModifierGenerator<T> setValues(List<ModifierValue<M>> values) {
        this.values.clear();

        if (values.isEmpty()) {
            this.values.add(new ModifierValue<>(attribute.type(), attribute.defaultValue()));
        } else {
            this.values.addAll(values);
        }

        return this;
    }

    public record ModifierValue<M>(RegisteredPersistentDataType<M> type, M value) {
        private <T> SpellAttributeModifier<T, M> createModifier(SpellAttribute<T> attribute, AttributeModifierType modifierType) {
            return attribute.createModifier(modifierType, type, value);
        }
    }
}
