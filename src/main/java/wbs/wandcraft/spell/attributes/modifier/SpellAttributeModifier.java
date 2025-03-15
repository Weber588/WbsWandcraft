package wbs.wandcraft.spell.attributes.modifier;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import wbs.wandcraft.ComponentRepresentable;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.util.CustomPersistentDataTypes;

public class SpellAttributeModifier<T> implements ComponentRepresentable {
    public static final NamespacedKey ATTRIBUTE_KEY = WbsWandcraft.getKey("attribute");
    public static final NamespacedKey MODIFIER_TYPE = WbsWandcraft.getKey("modifier");
    public static final NamespacedKey MODIFIER_VALUE = WbsWandcraft.getKey("modifier_value");

    private final SpellAttribute<T> attribute;
    private final AttributeModifierType type;
    private T modifierValue;

    public SpellAttributeModifier(SpellAttribute<T> attribute, AttributeModifierType type, T modifierValue) {
        this.attribute = attribute;
        this.type = type;
        this.modifierValue = modifierValue;
    }

    public SpellAttributeModifier(SpellAttributeInstance<T> attributeInstance, AttributeModifierType type) {
        this(attributeInstance.attribute(), type, attributeInstance.value());
    }

    public T modify(T value) {
        return type.modify(value, modifierValue);
    }

    public void modify(SpellInstance instance) {
        instance.applyModifier(this);
    }

    public SpellAttribute<T> attribute() {
        return attribute;
    }

    public AttributeModifierType type() {
        return type;
    }

    public void writeTo(PersistentDataContainer modifierContainer) {
        modifierContainer.set(MODIFIER_VALUE, attribute().type(), modifierValue);
        modifierContainer.set(ATTRIBUTE_KEY, CustomPersistentDataTypes.NAMESPACED_KEY, attribute().getKey());
        modifierContainer.set(MODIFIER_TYPE, CustomPersistentDataTypes.NAMESPACED_KEY, type().getKey());
    }

    public static SpellAttributeModifier<?> fromContainer(PersistentDataContainer container) {
        NamespacedKey attributeKey = container.get(ATTRIBUTE_KEY, CustomPersistentDataTypes.NAMESPACED_KEY);
        SpellAttribute<?> attribute = WandcraftRegistries.ATTRIBUTES.get(attributeKey);
        if (attribute == null) {
            throw new IllegalStateException("Attribute missing while generating attribute modifier.");
        }

        return attribute.createModifier(container);
    }


    @Override
    public Component toComponent() {
        return type.asComponent(attribute, modifierValue);
    }

    public T value() {
        return modifierValue;
    }

    public void value(T value) {
        this.modifierValue = value;
    }

    public void value(SpellAttributeModifier<T> modifierInstance) {
        value(modifierInstance.value());
    }
}
