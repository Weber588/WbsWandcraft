package wbs.wandcraft.generator;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.WbsCollectionUtil;
import wbs.wandcraft.RegisteredPersistentDataType;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WandcraftSettings;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class AttributeModifierGenerator<T> {
    @Nullable
    public static AttributeModifierGenerator<?> fromConfig(ConfigurationSection section,
                                                           WandcraftSettings settings,
                                                           String directory,
                                                           @NotNull AttributeModifierType fallbackType
    ) {
        String attributeString = section.getName();
        NamespacedKey attributeKey = NamespacedKey.fromString(attributeString, WbsWandcraft.getInstance());

        SpellAttribute<?> attribute = WandcraftRegistries.ATTRIBUTES.get(attributeKey);
        if (attribute == null) {
            settings.logError("Invalid attribute key \"" + attributeString + "\".", directory + "/" + attributeString);
        } else {
            String operatorString = section.getString("operator");
            if (operatorString == null) {
                operatorString = fallbackType.getKey().asString();
                settings.logError("Missing operator string! Defaulting to \"" + operatorString + "\".", directory + "/operator");
            }

            AttributeModifierType modifierType = WandcraftRegistries.MODIFIER_TYPES.get(NamespacedKey.fromString(operatorString, WbsWandcraft.getInstance()));
            if (modifierType == null) {
                modifierType = fallbackType;
                settings.logError("Invalid operator \"" + operatorString + "\". Defaulting to \"" + fallbackType.getKey().asString() + "\".", directory + "/operator");
            }

            return new AttributeModifierGenerator<>(
                    attribute,
                    modifierType,
                    section,
                    settings,
                    directory + "/" + attributeString
            );
        }

        return null;
    }

    private final SpellAttribute<T> attribute;
    private final AttributeModifierType modifierType;
    private final List<ModifierValue<?>> values = new LinkedList<>();

    public AttributeModifierGenerator(SpellAttribute<T> attribute, AttributeModifierType modifierType) {
        this.attribute = attribute;
        this.modifierType = modifierType;
        values.add(new ModifierValue<>(attribute.type(), attribute.defaultValue()));
    }

    public AttributeModifierGenerator(SpellAttribute<T> attribute,
                                      AttributeModifierType modifierType,
                                      ConfigurationSection generatorSection,
                                      WandcraftSettings settings,
                                      String directory
    ) {
        this(attribute, modifierType);

        List<?> values = generatorSection.getList("values");

        if (values != null && !values.isEmpty()) {
            Class<T> tClass = attribute.getTClass();
            List<ModifierValue<?>> valuesToAdd = new LinkedList<>();

            for (Object value : values) {
                boolean foundType = false;
                for (RegisteredPersistentDataType<?> type : WandcraftRegistries.DATA_TYPES.values()) {
                    Class<?> clazz = type.dataType().getComplexType();
                    if (clazz.isInstance(value)) {
                        valuesToAdd.add(ModifierValue.from(type, value));
                        foundType = true;
                        break;
                    }
                }

                if (!foundType) {
                    settings.logError("Value \"" + value + "\" is not valid for attribute of type " + tClass.getCanonicalName(), directory + "/values");
                }
            }

            if (!valuesToAdd.isEmpty()) {
                setValues(valuesToAdd);
            }
        }
    }

    public SpellAttributeModifier<T, ?> get() {
        return WbsCollectionUtil.getRandom(values).createModifier(attribute, modifierType);
    }

    @SafeVarargs
    public final <M> AttributeModifierGenerator<T> setValues(RegisteredPersistentDataType<M> modifierType, M... values) {
        List<ModifierValue<?>> list = new LinkedList<>();

        Arrays.stream(values)
                .map(value -> new ModifierValue<>(modifierType, value))
                .forEach(list::add);

        return setValues(list);
    }

    public AttributeModifierGenerator<T> setValues(List<ModifierValue<?>> values) {
        this.values.clear();

        if (values.isEmpty()) {
            this.values.add(new ModifierValue<>(attribute.type(), attribute.defaultValue()));
        } else {
            this.values.addAll(values);
        }

        return this;
    }

    public record ModifierValue<M>(RegisteredPersistentDataType<M> type, M value) {
        public static <T> ModifierValue<?> from(RegisteredPersistentDataType<T> type, Object value) {
            return new ModifierValue<>(type, type.dataType().getComplexType().cast(value));
        }

        private <T> SpellAttributeModifier<T, M> createModifier(SpellAttribute<T> attribute, AttributeModifierType modifierType) {
            return attribute.createModifier(modifierType, type, value);
        }
    }
}
