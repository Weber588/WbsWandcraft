package wbs.wandcraft.util;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEnums;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.SpellManager;
import wbs.wandcraft.spell.event.SpellEffectDefinition;
import wbs.wandcraft.spell.event.SpellEffectInstance;
import wbs.wandcraft.spell.modifier.ModifierScope;
import wbs.wandcraft.spell.modifier.SpellModifier;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class CustomPersistentDataTypes {
    public static final PersistentKeyType NAMESPACED_KEY = new PersistentKeyType();
    public static final PersistentSpellInstanceType SPELL_MODIFIER = new PersistentSpellInstanceType();
    public static final PersistentSpellInstanceType SPELL_INSTANCE = new PersistentSpellInstanceType();
    public static final PersistentAttributeModifierType SPELL_ATTRIBUTE_MODIFIER = new PersistentAttributeModifierType();

    public static class PersistentKeyType implements PersistentDataType<String, NamespacedKey> {
        @Override
        public @NotNull Class<String> getPrimitiveType() {
            return String.class;
        }

        @Override
        public @NotNull Class<NamespacedKey> getComplexType() {
            return NamespacedKey.class;
        }

        @Override
        public @NotNull String toPrimitive(@NotNull NamespacedKey namespacedKey, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
            return namespacedKey.asString();
        }

        @Override
        public @NotNull NamespacedKey fromPrimitive(@NotNull String s, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
            return Objects.requireNonNull(NamespacedKey.fromString(s));
        }
    }

    public static class PersistentSpellInstanceType implements PersistentDataType<PersistentDataContainer, SpellInstance> {
        private static final NamespacedKey ATTRIBUTES = WbsWandcraft.getKey( "attributes");
        private static final NamespacedKey DEFINITION = WbsWandcraft.getKey( "definition");

        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<SpellInstance> getComplexType() {
            return SpellInstance.class;
        }

        @Override
        public @NotNull PersistentDataContainer toPrimitive(@NotNull SpellInstance spellInstance, @NotNull PersistentDataAdapterContext context) {
            PersistentDataContainer container = context.newPersistentDataContainer();

            PersistentDataContainer attributes = context.newPersistentDataContainer();
            for (SpellAttributeInstance<?> attribute : spellInstance.getAttributes()) {
                attribute.writeTo(attributes);
            }

            container.set(ATTRIBUTES, PersistentDataType.TAG_CONTAINER, attributes);
            container.set(DEFINITION, NAMESPACED_KEY, spellInstance.getDefinition().getKey());

            return container;
        }

        @Override
        public @NotNull SpellInstance fromPrimitive(@NotNull PersistentDataContainer container, @NotNull PersistentDataAdapterContext context) {
            NamespacedKey definitionKey = container.get(DEFINITION, NAMESPACED_KEY);

            SpellDefinition definition = SpellManager.get(definitionKey);

            SpellInstance spellInstance = new SpellInstance(definition);

            PersistentDataContainer attributes = container.get(ATTRIBUTES, PersistentDataType.TAG_CONTAINER);
            if (attributes == null) {
                throw new IllegalStateException("Attributes field missing from spell instance PDC!");
            }
            for (SpellAttribute<?> attribute : definition.getAttributes()) {
                spellInstance.setAttribute(attribute.getInstance(attributes));
            }

            return spellInstance;
        }
    }

    public static class PersistentSpellModifierType implements PersistentDataType<PersistentDataContainer, SpellModifier> {
        private static final NamespacedKey ATTRIBUTE_MODIFIERS = WbsWandcraft.getKey( "attribute_modifiers");
        private static final NamespacedKey EFFECTS = WbsWandcraft.getKey( "effects");
        private static final NamespacedKey DEFINITION = WbsWandcraft.getKey( "definition");
        private static final NamespacedKey EFFECT_ATTRIBUTES = WbsWandcraft.getKey( "attributes");
        private static final NamespacedKey SCOPE = WbsWandcraft.getKey( "scope");

        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<SpellModifier> getComplexType() {
            return SpellModifier.class;
        }

        @Override
        public @NotNull PersistentDataContainer toPrimitive(@NotNull SpellModifier modifier, @NotNull PersistentDataAdapterContext context) {
            PersistentDataContainer container = context.newPersistentDataContainer();

            List<PersistentDataContainer> modifierContainerList = new LinkedList<>();
            for (SpellAttributeModifier<?> attributeModifier : modifier.getModifiers()) {
                PersistentDataContainer modifierContainer = context.newPersistentDataContainer();
                attributeModifier.writeTo(modifierContainer);
                modifierContainerList.add(modifierContainer);
            }
            container.set(ATTRIBUTE_MODIFIERS, PersistentDataType.LIST.dataContainers(), modifierContainerList);

            List<PersistentDataContainer> effectContainerList = new LinkedList<>();
            for (SpellEffectInstance<?> effect : modifier.getEffects()) {
                PersistentDataContainer effectContainer = context.newPersistentDataContainer();

                effectContainer.set(DEFINITION, NAMESPACED_KEY, effect.getDefinition().getKey());

                PersistentDataContainer attributes = context.newPersistentDataContainer();
                for (SpellAttributeInstance<?> attribute : effect.getAttributes()) {
                    attribute.writeTo(attributes);
                }

                effectContainer.set(EFFECT_ATTRIBUTES, PersistentDataType.TAG_CONTAINER, attributes);

                effectContainerList.add(effectContainer);
            }

            container.set(EFFECTS, PersistentDataType.LIST.dataContainers(), effectContainerList);

            container.set(SCOPE, new PersistentEnumType<>(ModifierScope.class), modifier.getScope());

            return container;
        }

        @Override
        public @NotNull SpellModifier fromPrimitive(@NotNull PersistentDataContainer container, @NotNull PersistentDataAdapterContext context) {
            ModifierScope modifierScope = container.get(SCOPE, new PersistentEnumType<>(ModifierScope.class));
            SpellModifier spellModifier = new SpellModifier(modifierScope);

            List<PersistentDataContainer> modifierContainerList = container.get(ATTRIBUTE_MODIFIERS, PersistentDataType.LIST.dataContainers());
            if (modifierContainerList == null) {
                throw new IllegalStateException("Deserialization of " + ATTRIBUTE_MODIFIERS.asString() + " missing!");
            }

            for (PersistentDataContainer modifierContainer : modifierContainerList) {
                SpellAttributeModifier<?> attributeModifier = SpellAttributeModifier.fromContainer(modifierContainer);

                spellModifier.addModifier(attributeModifier);
            }

            List<PersistentDataContainer> effectContainerList = container.get(EFFECTS, PersistentDataType.LIST.dataContainers());
            if (effectContainerList == null) {
                throw new IllegalStateException("Deserialization of " + EFFECTS.asString() + " missing!");
            }

            for (PersistentDataContainer effectContainer : effectContainerList) {
                NamespacedKey definitionKey = effectContainer.get(DEFINITION, NAMESPACED_KEY);
                SpellEffectDefinition<?> definition = WandcraftRegistries.EFFECTS.get(definitionKey);

                SpellEffectInstance<?> effectInstance = new SpellEffectInstance<>(definition);

                PersistentDataContainer attributesContainer = effectContainer.get(EFFECT_ATTRIBUTES, PersistentDataType.TAG_CONTAINER);
                if (attributesContainer != null) {
                    for (NamespacedKey attributeKey : attributesContainer.getKeys()) {
                        SpellAttribute<?> attribute = Objects.requireNonNull(
                                WandcraftRegistries.ATTRIBUTES.get(attributeKey),
                                "Attribute key was not present in registry! This is a bug. " + attributeKey.asString()
                        );
                        SpellAttributeInstance<?> instance = attribute.getInstance(attributesContainer);

                        effectInstance.addAttribute(instance);
                    }
                }

                spellModifier.addEffect(effectInstance);
            }

            return spellModifier;
        }
    }

    @SuppressWarnings("rawtypes")
    public static class PersistentAttributeModifierType implements PersistentDataType<PersistentDataContainer, SpellAttributeModifier> {

        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<SpellAttributeModifier> getComplexType() {
            return SpellAttributeModifier.class;
        }

        @Override
        public @NotNull PersistentDataContainer toPrimitive(@NotNull SpellAttributeModifier modifier, @NotNull PersistentDataAdapterContext context) {
            PersistentDataContainer modifierContainer = context.newPersistentDataContainer();

            modifier.writeTo(modifierContainer);

            return modifierContainer;
        }

        @Override
        public @NotNull SpellAttributeModifier<?> fromPrimitive(@NotNull PersistentDataContainer container, @NotNull PersistentDataAdapterContext context) {
            return SpellAttributeModifier.fromContainer(container);
        }
    }

    public static class PersistentEnumType<T extends Enum<T>> implements PersistentDataType<String, T> {
        private final Class<T> clazz;

        public PersistentEnumType(Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public @NotNull Class<String> getPrimitiveType() {
            return String.class;
        }

        @Override
        public @NotNull Class<T> getComplexType() {
            return clazz;
        }

        @Override
        public @NotNull String toPrimitive(@NotNull T enumValue, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
            return enumValue.name();
        }

        @Override
        public @NotNull T fromPrimitive(@NotNull String asString, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
            T enumFromString = WbsEnums.getEnumFromString(clazz, asString);

            if (enumFromString == null) {
                throw new IllegalStateException("Enum value not found! " + clazz.getCanonicalName() + ": " + asString);
            }

            return enumFromString;
        }
    }
}
