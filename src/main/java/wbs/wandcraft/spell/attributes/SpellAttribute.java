package wbs.wandcraft.spell.attributes;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.util.CustomPersistentDataTypes;

public class SpellAttribute<T> implements Keyed {
    private final NamespacedKey key;
    private final PersistentDataType<?, T> type;
    private final T defaultValue;

    public SpellAttribute(NamespacedKey key, PersistentDataType<?, T> type, T defaultValue) {
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;

        WandcraftRegistries.ATTRIBUTES.register(this);
    }

    public SpellAttribute(@Subst("key") String nativeKey, PersistentDataType<?, T> type, T defaultValue) {
        this(WbsWandcraft.getKey(nativeKey), type, defaultValue);
    }

    public SpellAttributeInstance<T> getInstance() {
        return getInstance(defaultValue);
    }

    public SpellAttributeInstance<T> getInstance(@NotNull T value) {
        return new SpellAttributeInstance<>(this, value);
    }


    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public SpellAttributeInstance<T> getInstance(PersistentDataContainer attributes) {
        T value = attributes.get(key, type);

        if (value == null) {
            value = defaultValue;
        }

        return getInstance(value);
    }

    public PersistentDataType<?, T> type() {
        return type;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public SpellAttributeModifier<T> createModifier(PersistentDataContainer container) {
        NamespacedKey typeKey = container.get(SpellAttributeModifier.MODIFIER_TYPE, CustomPersistentDataTypes.NAMESPACED_KEY);
        AttributeModifierType type = WandcraftRegistries.MODIFIER_TYPES.get(typeKey);

        T value = container.get(SpellAttributeModifier.MODIFIER_VALUE, type());

        return new SpellAttributeModifier<>(this, type, value);
    }
}
