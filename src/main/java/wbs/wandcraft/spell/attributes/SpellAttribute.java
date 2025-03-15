package wbs.wandcraft.spell.attributes;

import com.mojang.brigadier.arguments.ArgumentType;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.utils.util.string.WbsStrings;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.util.CustomPersistentDataTypes;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Function;

public class SpellAttribute<T> implements Keyed, Comparable<SpellAttribute<?>> {
    @NotNull
    private final NamespacedKey key;
    @NotNull
    private final PersistentDataType<?, T> type;
    @NotNull
    private final ArgumentType<T> argumentType;
    private final T defaultValue;
    @NotNull
    private final Function<String, T> parse;
    @NotNull
    private final Collection<T> suggestions = new HashSet<>();

    public SpellAttribute(@NotNull NamespacedKey key, @NotNull PersistentDataType<?, T> type, @NotNull ArgumentType<T> argumentType, T defaultValue, @NotNull Function<String, T> parse) {
        this.key = key;
        this.type = type;
        this.argumentType = argumentType;
        this.defaultValue = defaultValue;
        this.parse = parse;
        this.suggestions.add(defaultValue);

        WandcraftRegistries.ATTRIBUTES.register(this);
    }

    public SpellAttribute(@Subst("key") String nativeKey, PersistentDataType<?, T> type, ArgumentType<T> argumentType, T defaultValue, @NotNull Function<String, T> parse) {
        this(WbsWandcraft.getKey(nativeKey), type, argumentType, defaultValue, parse);
    }

    @SafeVarargs
    public final SpellAttribute<T> addSuggestions(T... suggestions) {
        return addSuggestions(Arrays.asList(suggestions));
    }

    public SpellAttribute<T> addSuggestions(Collection<T> suggestions) {
        this.suggestions.addAll(suggestions);
        return this;
    }

    @SafeVarargs
    public final SpellAttribute<T> setSuggestions(T... suggestions) {
        return setSuggestions(Arrays.asList(suggestions));
    }

    public SpellAttribute<T> setSuggestions(Collection<T> suggestions) {
        this.suggestions.clear();
        this.suggestions.addAll(suggestions);
        return this;
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

    public @NotNull ArgumentType<T> getArgumentType() {
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

    public T parse(String stringValue) {
        return parse.apply(stringValue);
    }

    public SpellAttributeInstance<T> getParsedInstance(String stringValue) {
        return getInstance(parse(stringValue));
    }

    @NotNull
    public Collection<T> getSuggestions() {
        return suggestions;
    }

    public Class<T> getTClass() {
        return type.getComplexType();
    }

    public int compareTo(@NotNull SpellAttribute<?> other) {
        return other.getKey().compareTo(getKey());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpellAttribute<?> that)) return false;
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(key);
    }
}
