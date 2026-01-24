package wbs.wandcraft.spell.attributes;

import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Color;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsColours;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.utils.util.string.WbsStrings;
import wbs.wandcraft.RegisteredPersistentDataType;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.resourcepack.TextureLayer;
import wbs.wandcraft.resourcepack.DynamicItemTextureProvider;
import wbs.wandcraft.spell.attributes.modifier.AttributeModificationOperator;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;

import java.text.DecimalFormat;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

@NullMarked
public class SpellAttribute<T> implements Keyed, Comparable<SpellAttribute<?>>, DynamicItemTextureProvider {
    @NotNull
    private final NamespacedKey key;
    @NotNull
    private Component displayName;
    @NotNull
    private final RegisteredPersistentDataType<T> type;
    @Nullable
    private final T defaultValue;
    @NotNull
    private final Function<String, T> parse;
    @NotNull
    private final Collection<T> suggestions = new HashSet<>();
    private BiFunction<@NotNull T, Attributable, @NotNull Boolean> shouldShow = (value, attributable) -> true;
    private Function<@NotNull T, @NotNull String> formatter = Objects::toString;
    private final List<TypedFormatter<?>> typedFormatters = new LinkedList<>();
    private boolean isWritable = false;
    private String textureValue;
    private Polarity polarity = Polarity.POSITIVE;

    public SpellAttribute(@NotNull NamespacedKey key, @NotNull RegisteredPersistentDataType<T> type, @Nullable T defaultValue, @NotNull Function<String, T> parse) {
        this.key = key;
        this.displayName = Component.text(WbsStrings.capitalizeAll(key.value().replaceAll("_", " ")));
        this.type = type;
        this.defaultValue = defaultValue;
        this.parse = parse;
        if (defaultValue != null) {
            this.suggestions.add(defaultValue);
        }
        textureValue = key.value();

        setFormatter(formatter);
        WandcraftRegistries.ATTRIBUTES.register(this);
    }

    public SpellAttribute(@Subst("key") String nativeKey, RegisteredPersistentDataType<T> type, @Nullable T defaultValue, @NotNull Function<String, T> parse) {
        this(WbsWandcraft.getKey(nativeKey), type, defaultValue, parse);
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

    public SpellAttributeInstance<T> getInstance(@Nullable T value) {
        return new SpellAttributeInstance<>(this, value);
    }


    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public SpellAttributeInstance<T> getInstance(PersistentDataContainer attributes) {
        T value;
        if (defaultValue != null) {
            value = attributes.getOrDefault(key, type.dataType(), defaultValue);
        } else {
            value = attributes.get(key, type.dataType());
        }

        return getInstance(value);
    }

    public RegisteredPersistentDataType<T> type() {
        return type;
    }

    @Nullable
    public T defaultValue() {
        return defaultValue;
    }

    public SpellAttribute<T> setShowAttribute(Function<@NotNull T, @NotNull Boolean> shouldShow) {
        return setShowAttribute((value, ignored) -> shouldShow.apply(value));
    }
    public SpellAttribute<T> setShowAttribute(BiFunction<@NotNull T, Attributable, @NotNull Boolean> shouldShow) {
        this.shouldShow = shouldShow;
        return this;
    }

    public boolean shouldShow(T value, Attributable attributable) {
        return shouldShow.apply(value, attributable);
    }

    public SpellAttribute<T> setFormatter(Function<T, String> formatter) {
        this.formatter = formatter;
        this.typedFormatters.add(new TypedFormatter<>(type, formatter));
        return this;
    }

    public <M> SpellAttribute<T> addTypedFormatter(RegisteredPersistentDataType<M> dataType, Function<M, String> formatter) {
        this.typedFormatters.add(new TypedFormatter<>(dataType, formatter));
        if (dataType.dataType().getComplexType() == type.dataType().getComplexType()) {
            //noinspection unchecked
            this.formatter = (Function<T, String>) formatter;
        }
        return this;
    }

    public SpellAttribute<T> setNumericFormatter(Function<String, String> numericFormatter) {
        return setNumericFormatter(1, numericFormatter);
    }
    public SpellAttribute<T> setNumericFormatter(double multiplicationFactor, Function<String, String> numericFormatter) {
        DecimalFormat decimalFormat = new DecimalFormat("0.##");

        addTypedFormatter(RegisteredPersistentDataType.INTEGER,
                value -> numericFormatter.apply(decimalFormat.format(multiplicationFactor * value))
        );
        addTypedFormatter(RegisteredPersistentDataType.DOUBLE,
                value -> numericFormatter.apply(decimalFormat.format(multiplicationFactor * value))
        );
        addTypedFormatter(RegisteredPersistentDataType.LONG,
                value -> numericFormatter.apply(decimalFormat.format(multiplicationFactor * value))
        );

        return this;
    }

    public SpellAttribute<T> setTicksToSecondsFormatter() {
        return setNumericFormatter(1d / Ticks.TICKS_PER_SECOND, value -> {
            if (value.equals("1")) {
                return "1 second";
            }

            return value + " seconds";
        });
    }

    public String formatValue(@Nullable T value) {
        if (value == null) {
            return "null";
        }
        return formatter.apply(value);
    }

    public String formatAny(@Nullable Object value) {
        if (value == null) {
            return "null";
        }

        for (TypedFormatter<?> formatter : typedFormatters) {
            if (formatter.dataType.dataType().getComplexType() == value.getClass()) {
                return formatter.format(value);
            }
        }

        return "FORMATTER_ERROR -- " + value + " (" + value.getClass().getName() + ")";
    }

        public <M> SpellAttributeModifier<T, M> createModifier(PersistentDataContainerView container, RegisteredPersistentDataType<M> modifierType) {
        M value =  container.get(SpellAttributeModifier.MODIFIER_VALUE, modifierType.dataType());

        NamespacedKey operationTypeKey = container.get(SpellAttributeModifier.MODIFIER_OPERATION, WbsPersistentDataType.NAMESPACED_KEY);
        AttributeModifierType definition = WandcraftRegistries.MODIFIER_TYPES.get(operationTypeKey);
        if (definition == null) {
            throw new IllegalStateException("Invalid or missing data type for modifier.");
        }

        AttributeModificationOperator<T, M> operator = definition.buildModifierType(type.dataType(), modifierType);

        return new SpellAttributeModifier<>(this, operator, value);
    }

    public <M> SpellAttributeModifier<T, M> createModifier(
            AttributeModifierType modifierDefinition,
            RegisteredPersistentDataType<M> modifierDataType,
            @Nullable M value
    ) {
        AttributeModificationOperator<T, M> operator = modifierDefinition.buildModifierType(type.dataType(), modifierDataType);

        return new SpellAttributeModifier<>(this, operator, value);
    }

    @NotNull
    public Component displayName() {
        return displayName;
    }

    public SpellAttribute<T> displayName(@NotNull Component displayName) {
        this.displayName = displayName;
        return this;
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
        return type.dataType().getComplexType();
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

    @Nullable
    public SpellAttributeInstance<T> defaultInstance() {
        return getInstance(defaultValue);
    }

    public boolean isWritable() {
        return isWritable;
    }

    public SpellAttribute<T> setWritable(boolean writable) {
        isWritable = writable;
        return this;
    }

    @Override
    public @NotNull List<TextureLayer> getTextures() {
        return List.of(
                new TextureLayer("modifier_overlay", false, 0xEC273F),
                new TextureLayer("modifier_" + textureValue)
        );
    }

    public SpellAttribute<T> overrideTextureValue(String textureValue) {
        this.textureValue = textureValue;
        return this;
    }

    public Polarity polarity() {
        return polarity;
    }

    @Nullable
    public TextColor getPolarityColor(boolean isPositive) {
        if (isPositive) {
            return polarity.color();
        } else {
            return polarity.invert().color();
        }
    }

    public Polarity getPolarity(T value) {
        return polarity;
    }

    public SpellAttribute<T> polarity(Polarity polarity) {
        this.polarity = polarity;
        return this;
    }

    private record TypedFormatter<M>(RegisteredPersistentDataType<M> dataType, Function<@Nullable M, String> formatter) {
        public String format(Object value) {
            Class<M> complexType = dataType.dataType().getComplexType();
            if (complexType != (value.getClass())) {
                throw new IllegalArgumentException("Typed formatter for type " + complexType.getName()
                        + " cannot format value of type " + value.getClass().getName()
                );
            }

            //noinspection unchecked
            return formatter.apply((M) value);
        }
    }

    public enum Polarity {
        POSITIVE(NamedTextColor.AQUA, WbsColours.fromHSB(0.675, 0.5, 0.6)),
        NEGATIVE(NamedTextColor.RED, WbsColours.fromHSB(0.958, 0.85, 1)),
        NEUTRAL(WbsColours.fromHSB(0.025, 0.6, 0.75));

        @Nullable
        private NamedTextColor color;
        private final Color scrollColor;

        Polarity(Color scrollColor) {

            this.scrollColor = scrollColor;
        }
        Polarity(NamedTextColor color, Color scrollColor) {
            this.color = color;
            this.scrollColor = scrollColor;
        }

        public @Nullable NamedTextColor color() {
            return color;
        }

        public Color getScrollColor() {
            return scrollColor;
        }

        public Polarity invert() {
            return switch (this) {
                case POSITIVE -> NEGATIVE;
                case NEGATIVE -> POSITIVE;
                case NEUTRAL -> NEUTRAL;
            };
        }

        public Polarity multiply(Polarity other) {
            return switch (this) {
                case POSITIVE -> other;
                case NEGATIVE -> other.invert();
                case NEUTRAL -> NEUTRAL;
            };
        }
    }
}
