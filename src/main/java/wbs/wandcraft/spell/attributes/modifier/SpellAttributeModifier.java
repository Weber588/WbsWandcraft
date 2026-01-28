package wbs.wandcraft.spell.attributes.modifier;

import io.papermc.paper.persistence.PersistentDataContainerView;
import io.papermc.paper.persistence.PersistentDataViewHolder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.wandcraft.ComponentRepresentable;
import wbs.wandcraft.RegisteredPersistentDataType;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.SpellInstance;

public class SpellAttributeModifier<T, M> implements ComponentRepresentable {
    public static final NamespacedKey ATTRIBUTE_KEY = WbsWandcraft.getKey("attribute");
    public static final NamespacedKey MODIFIER_OPERATION = WbsWandcraft.getKey("modifier_operation");
    public static final NamespacedKey MODIFIER_TYPE = WbsWandcraft.getKey("modifier_type");
    public static final NamespacedKey MODIFIER_VALUE = WbsWandcraft.getKey("modifier_value");

    private final SpellAttribute<T> attribute;
    private final @NotNull AttributeModificationOperator<T, M> modifierOperation;
    @Nullable
    private M modifierValue;

    public SpellAttributeModifier(SpellAttribute<T> attribute, @NotNull AttributeModificationOperator<T, M> modifierOperation, @Nullable M modifierValue) {
        this.attribute = attribute;
        this.modifierOperation = modifierOperation;
        this.modifierValue = modifierValue;
    }

    @SuppressWarnings("unchecked")
    public T modify(T value) {
        T modified = modifierOperation.modify(value, modifierValue);

        if (value instanceof Number && modified instanceof Number modifiedNumber) {
            return switch (value) {
                case Double ignored -> (T) Double.valueOf(modifiedNumber.doubleValue());
                case Integer ignored -> (T) Integer.valueOf(modifiedNumber.intValue());
                case Float ignored -> (T) Float.valueOf(modifiedNumber.floatValue());
                case Long ignored -> (T) Long.valueOf(modifiedNumber.longValue());
                default -> modified;
            };
        }

        return modified;
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
        modifierContainer.set(ATTRIBUTE_KEY, WbsPersistentDataType.NAMESPACED_KEY, attribute().getKey());

        modifierContainer.set(MODIFIER_TYPE, WbsPersistentDataType.NAMESPACED_KEY, modifierOperation.getModifierType().getKey());
        if (modifierValue != null) {
            modifierContainer.set(MODIFIER_VALUE, modifierOperation.getModifierType().dataType(), modifierValue);
        } else {
            modifierContainer.remove(MODIFIER_VALUE);
        }

        modifierContainer.set(MODIFIER_OPERATION, WbsPersistentDataType.NAMESPACED_KEY, modifierOperation.getDefinition().getKey());
    }

    public static SpellAttributeModifier<?, ?> fromContainer(@NotNull PersistentDataContainerView container) {
        NamespacedKey attributeKey = container.get(ATTRIBUTE_KEY, WbsPersistentDataType.NAMESPACED_KEY);

        if (attributeKey == null) {
            return null;
        }

        SpellAttribute<?> attribute = WandcraftRegistries.ATTRIBUTES.get(attributeKey);
        if (attribute == null) {
            throw new IllegalStateException("Attribute missing while generating attribute modifier.");
        }

        NamespacedKey typeKey = container.get(SpellAttributeModifier.MODIFIER_TYPE, WbsPersistentDataType.NAMESPACED_KEY);
        RegisteredPersistentDataType<?> modifierType = WandcraftRegistries.DATA_TYPES.get(typeKey);
        if (modifierType == null) {
            throw new IllegalStateException("Invalid or missing data type for modifier.");
        }

        return attribute.createModifier(container, modifierType);
    }

    public static SpellAttributeModifier<?, ?> fromItem(PersistentDataViewHolder holder) {
        if (holder == null) {
            return null;
        }
        return fromContainer(holder.getPersistentDataContainer());
    }

    @Override
    public Component toComponent() {
        NamedTextColor color = getSentiment().color();

        return attribute.displayName().append(
                modifierOperation.asComponent(attribute, modifierValue)
                        .color(color)
        );
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

    public SpellAttribute.Sentiment getSentiment() {
        return modifierOperation.getSentiment(modifierValue).multiply(attribute.sentiment());
    }
}
