package wbs.wandcraft.spell.attributes.modifier;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.ComponentRepresentable;
import wbs.wandcraft.RegisteredPersistentDataType;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.util.CustomPersistentDataTypes;

public class SpellAttributeModifier<T, M> implements ComponentRepresentable {
    public static final NamespacedKey ATTRIBUTE_KEY = WbsWandcraft.getKey("attribute");
    public static final NamespacedKey MODIFIER_OPERATION = WbsWandcraft.getKey("modifier_operation");
    public static final NamespacedKey MODIFIER_TYPE = WbsWandcraft.getKey("modifier_type");
    public static final NamespacedKey MODIFIER_VALUE = WbsWandcraft.getKey("modifier_value");

    private final SpellAttribute<T> attribute;
    private final @NotNull AttributeModificationOperator<T, M> modifierOperation;
    private M modifierValue;

    public SpellAttributeModifier(SpellAttribute<T> attribute, @NotNull AttributeModificationOperator<T, M> modifierOperation, M modifierValue) {
        this.attribute = attribute;
        this.modifierOperation = modifierOperation;
        this.modifierValue = modifierValue;
    }

    public T modify(T value) {
        return modifierOperation.modify(value, modifierValue);
    }

    public void modify(SpellInstance instance) {
        instance.applyModifier(this);
    }

    public SpellAttribute<T> attribute() {
        return attribute;
    }

    public @NotNull AttributeModificationOperator<T, M> operator() {
        return modifierOperation;
    }

    public void writeTo(PersistentDataContainer modifierContainer) {
        modifierContainer.set(ATTRIBUTE_KEY, CustomPersistentDataTypes.NAMESPACED_KEY, attribute().getKey());

        modifierContainer.set(MODIFIER_TYPE, CustomPersistentDataTypes.NAMESPACED_KEY, modifierOperation.getModifierType().getKey());
        modifierContainer.set(MODIFIER_VALUE, modifierOperation.getModifierType().dataType(), modifierValue);

        modifierContainer.set(MODIFIER_OPERATION, CustomPersistentDataTypes.NAMESPACED_KEY, modifierOperation.getDefinition().getKey());
    }

    public static SpellAttributeModifier<?, ?> fromContainer(PersistentDataContainer container) {
        NamespacedKey attributeKey = container.get(ATTRIBUTE_KEY, CustomPersistentDataTypes.NAMESPACED_KEY);
        SpellAttribute<?> attribute = WandcraftRegistries.ATTRIBUTES.get(attributeKey);
        if (attribute == null) {
            throw new IllegalStateException("Attribute missing while generating attribute modifier.");
        }

        NamespacedKey typeKey = container.get(SpellAttributeModifier.MODIFIER_TYPE, CustomPersistentDataTypes.NAMESPACED_KEY);
        RegisteredPersistentDataType<?> modifierType = WandcraftRegistries.DATA_TYPES.get(typeKey);
        if (modifierType == null) {
            throw new IllegalStateException("Invalid or missing data type for modifier.");
        }

        return attribute.createModifier(container, modifierType);
    }


    @Override
    public Component toComponent() {
        return modifierOperation.asComponent(attribute, modifierValue);
    }

    public M value() {
        return modifierValue;
    }

    public void value(M value) {
        this.modifierValue = value;
    }

    public void value(SpellAttributeModifier<T, M> modifierInstance) {
        value(modifierInstance.value());
    }
}
