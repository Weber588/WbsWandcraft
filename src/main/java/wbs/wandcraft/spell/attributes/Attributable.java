package wbs.wandcraft.spell.attributes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.ItemDecorator;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;

import java.util.List;
import java.util.Set;

public interface Attributable extends ItemDecorator {
    Set<SpellAttributeInstance<?>> getAttributeValues();

    default <T> void addAttribute(SpellAttribute<T> attribute, T value) {
        addAttribute(attribute.getInstance(value));
    }

    default void addAttribute(SpellAttributeInstance<?> instance) {
        getAttributeValues().removeIf(existing -> existing.attribute().equals(instance.attribute()));
        getAttributeValues().add(instance.clone());
    }

    default <T> void setAttribute(SpellAttribute<T> attribute, T value) {
        for (SpellAttributeInstance<?> attributeValue : getAttributeValues()) {
            if (attributeValue.attribute().equals(attribute)) {
                attributeValue.value(value);
                return;
            }
        }
    }

    default  <T> T getAttribute(SpellAttribute<T> attribute) {
        //noinspection unchecked
        SpellAttributeInstance<T> instance =
                (SpellAttributeInstance<T>) getAttributeValues().stream()
                        .filter(value -> value.attribute().equals(attribute))
                        .findFirst()
                        .orElse(null);

        if (instance == null) {
            return attribute.defaultValue();
        }

        return instance.value();
    }

    @Contract("_, !null -> !null")
    default <T> T getAttribute(SpellAttribute<T> attribute, T defaultValue) {
        T attributeValue = getAttribute(attribute);
        if (attributeValue == null) {
            return defaultValue;
        }

        return attributeValue;
    }

    default  <T> void setAttribute(SpellAttributeInstance<T> instance) {
        setAttribute(instance.attribute(), instance.value());
    }

    default  <T> void applyModifier(SpellAttributeModifier<T, ?> modifier) {
        for (SpellAttributeInstance<?> attributeInstance : getAttributeValues()) {
            if (attributeInstance.attribute().equals(modifier.attribute())) {
                attributeInstance.modify(modifier);
            }
        }
    }

    default void writeAttributes(PersistentDataContainer container, NamespacedKey key) {
        PersistentDataContainer attributes = container.getAdapterContext().newPersistentDataContainer();
        for (SpellAttributeInstance<?> attribute : getAttributeValues()) {
            attribute.writeTo(attributes);
        }

        container.set(key, PersistentDataType.TAG_CONTAINER, attributes);
    }

    default void readAttributes(PersistentDataContainer container, NamespacedKey key) {
        PersistentDataContainer attributes = container.get(key, PersistentDataType.TAG_CONTAINER);
        if (attributes == null) {
            throw new IllegalStateException("Attributes field missing from spell instance PDC!");
        }

        for (NamespacedKey attributesKey : attributes.getKeys()) {
            SpellAttribute<?> attribute = WandcraftRegistries.ATTRIBUTES.get(attributesKey);
            if (attribute == null) {
                WbsWandcraft.getInstance().getLogger().warning("An unrecognised attribute key was provided: " + attributesKey.asString());
                continue;
            }
            setAttribute(attribute.getInstance(attributes));
        }
    }

    @Override
    default @NotNull List<Component> getLore() {
        return getAttributeValues().stream()
                .sorted()
                .filter(SpellAttributeInstance::shouldShow)
                .map(instance ->
                        (Component) Component.text("  - ").style(Style.style(NamedTextColor.GOLD, Set.of()))
                                .append(instance.toComponent())
                )
                .toList();
    }

}
