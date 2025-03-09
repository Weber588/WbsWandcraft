package wbs.wandcraft.spell.attributes;

import com.mojang.brigadier.arguments.ArgumentType;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.utils.util.string.WbsStrings;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.util.CustomPersistentDataTypes;

@NullMarked
public class SpellAttribute<T> implements Keyed {
    private final NamespacedKey key;
    private final PersistentDataType<?, T> type;
    private final ArgumentType<T> argumentType;
    private final T defaultValue;

    public SpellAttribute(NamespacedKey key, PersistentDataType<?, T> type, ArgumentType<T> argumentType, T defaultValue) {
        this.key = key;
        this.type = type;
        this.argumentType = argumentType;
        this.defaultValue = defaultValue;

        WandcraftRegistries.ATTRIBUTES.register(this);
    }

    public SpellAttribute(@Subst("key") String nativeKey, PersistentDataType<?, T> type, ArgumentType<T> argumentType, T defaultValue) {
        this(WbsWandcraft.getKey(nativeKey), type, argumentType, defaultValue);
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
        T value = WbsPersistentDataType.getOrDefault(attributes, key, type, defaultValue);

        return getInstance(value);
    }

    public PersistentDataType<?, T> type() {
        return type;
    }

    public ArgumentType<T> getArgumentType() {
        return argumentType;
    }

    public T defaultValue() {
        return defaultValue;
    }

    public SpellAttributeModifier<T> createModifier(PersistentDataContainer container) {
        NamespacedKey typeKey = container.get(SpellAttributeModifier.MODIFIER_TYPE, CustomPersistentDataTypes.NAMESPACED_KEY);
        AttributeModifierType type = WandcraftRegistries.MODIFIER_TYPES.get(typeKey);

        T value = WbsPersistentDataType.getOrDefault(container, SpellAttributeModifier.MODIFIER_VALUE, type(), defaultValue);

        return new SpellAttributeModifier<>(this, type, value);
    }

    public WbsSimpleArgument<T> getArg() {
        //noinspection unchecked
        return new WbsSimpleArgument<>(
                getKey().asString(),
                getArgumentType(),
                defaultValue(),
                (Class<T>) defaultValue().getClass()
        ).setTooltip(getKey().value());
    }

    public Component displayName() {
        return Component.text(WbsStrings.capitalizeAll(key.value().replaceAll("_", " ")));
    }
}
