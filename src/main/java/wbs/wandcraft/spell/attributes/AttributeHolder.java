package wbs.wandcraft.spell.attributes;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.*;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.attributable.AttributableSetupHandler;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.util.ItemDecorator;
import wbs.wandcraft.util.MenuUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public interface AttributeHolder extends ItemDecorator {
    /**
     * @return The actual underlying set containing stored attribute instances. Mutable & modifiable.
     */
    Set<SpellAttributeInstance<?>> getAttributeInstances();

    /**
     * Implementors may override this method to self-apply modifiers, without changing what's stored or
     * used in internal attribute calculations.
     * @return A derived set of attribute instances to be used in reality, but not stored.
     */
    @Unmodifiable
    default Set<SpellAttributeInstance<?>> deriveAttributeValues() {
        return Collections.unmodifiableSet(getAttributeInstances());
    }

    default <T> void setAttribute(SpellAttribute<T> attribute, T value) {
        setAttribute(attribute.getInstance(value));
    }

    default void setAttribute(SpellAttribute<Integer> attribute, double value) {
        setAttribute(attribute.getInstance((int) value));
    }

    default void setAttribute(@Nullable SpellAttributeInstance<?> instance) {
        if (instance == null) {
            return;
        }
        getAttributeInstances().removeIf(existing -> existing.attribute().equals(instance.attribute()));
        getAttributeInstances().add(instance.clone());
    }

    default boolean hasAttribute(SpellAttribute<?> attribute) {
        return deriveAttributeValues().stream()
                .anyMatch(inst -> inst.attribute().equals(attribute));
    }

    @UnknownNullability
    default <T> T getAttribute(SpellAttribute<T> attribute) {
        //noinspection unchecked
        SpellAttributeInstance<T> instance =
                (SpellAttributeInstance<T>) deriveAttributeValues().stream()
                        .filter(val -> val.attribute().equals(attribute))
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

    default <T> void applyModifier(SpellAttributeModifier<T, ?> modifier) {
        for (SpellAttributeInstance<?> attributeInstance : getAttributeInstances()) {
            if (attributeInstance.attribute().equals(modifier.attribute())) {
                attributeInstance.modify(modifier);
            }
        }
    }

    default void writeAttributes(PersistentDataContainer container, NamespacedKey key) {
        PersistentDataContainer attributes = container.getAdapterContext().newPersistentDataContainer();
        for (SpellAttributeInstance<?> attribute : getAttributeInstances()) {
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
        return deriveAttributeValues().stream()
                .sorted()
                .filter(instance -> instance.shouldShow(this))
                .map(instance ->
                        (Component) Component.text("  - ").style(MenuUtils.EXTRAS_STYLE)
                                .append(instance.toComponent())
                )
                .toList();
    }

    default void addAttribute(SpellAttribute<?> attribute) {
        SpellAttributeInstance<?> instance = attribute.defaultInstance();
        if (instance == null) {
            WbsWandcraft.getInstance().debug(SpellAttribute.DEBUG_CHANNEL_ATTRIBUTES, "Null default instance passed to SpellDefinition#addAttribute");
        } else {
            setAttribute(instance);
        }
    }

    default Collection<SpellAttribute<?>> getAttributes() {
        return new LinkedList<>(getAttributeInstances().stream().map(SpellAttributeInstance::attribute).toList());
    }

    default void setupAttributables() {
        Class<? extends AttributeHolder> aClass = this.getClass();
        Map<String, Method> handlers = getSetupHandlers(aClass);

        for (Method handler : handlers.values()) {
            try {
                handler.invoke(this);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Map<String, Method> getSetupHandlers(Class<?> holderClass) {
        // Use a map based on name to prevent the same method being added from overridden methods
        Map<String, Method> attributableSetupHandlers = new HashMap<>();

        for (Class<?> implemented : holderClass.getInterfaces()) {
            // Loop through parents first, so we can override it below (lowest child implementation wins)
            attributableSetupHandlers.putAll(getSetupHandlers(implemented));

            if (!AttributeHolder.class.isAssignableFrom(implemented)) {
                continue;
            }

            for (Method m : implemented.getDeclaredMethods()) {
                if (m.isAnnotationPresent(AttributableSetupHandler.class)) {
                    attributableSetupHandlers.put(m.getName(), m);
                }
            }
        }

        return attributableSetupHandlers;
    }
}
