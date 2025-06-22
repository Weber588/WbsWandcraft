package wbs.wandcraft.util;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.effects.StatusEffectInstance;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.event.SpellEffectDefinition;
import wbs.wandcraft.spell.event.SpellEffectInstance;
import wbs.wandcraft.spell.modifier.ModifierScope;
import wbs.wandcraft.spell.modifier.SpellModifier;
import wbs.wandcraft.wand.Wand;
import wbs.wandcraft.wand.WandInventoryType;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CustomPersistentDataTypes {
    public static final PersistentSpellModifierType SPELL_MODIFIER = new PersistentSpellModifierType();
    public static final PersistentSpellInstanceType SPELL_INSTANCE = new PersistentSpellInstanceType();
    public static final PersistentAttributeModifierType SPELL_ATTRIBUTE_MODIFIER = new PersistentAttributeModifierType();
    public static final PersistentWandType WAND = new PersistentWandType();
    public static final PersistentStatusEffectType STATUS_EFFECT = new PersistentStatusEffectType();

    public static class PersistentStatusEffectType implements PersistentDataType<PersistentDataContainer, StatusEffectInstance> {
        private static final NamespacedKey EFFECT_TYPE = WbsWandcraft.getKey("type");
        private static final NamespacedKey INITIAL_TIME = WbsWandcraft.getKey("initial_time");
        private static final NamespacedKey TIME_LEFT = WbsWandcraft.getKey("time_left");
        private static final NamespacedKey SHOW_BOSS_BAR = WbsWandcraft.getKey("show_boss_bar");
        private static final NamespacedKey CAUSE = WbsWandcraft.getKey("cause");

        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<StatusEffectInstance> getComplexType() {
            return StatusEffectInstance.class;
        }

        @Override
        public @NotNull PersistentDataContainer toPrimitive(@NotNull StatusEffectInstance instance, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
            PersistentDataContainer container = persistentDataAdapterContext.newPersistentDataContainer();

            container.set(EFFECT_TYPE, WbsPersistentDataType.NAMESPACED_KEY, instance.getEffect().getKey());
            container.set(INITIAL_TIME, PersistentDataType.INTEGER, instance.getInitialTime());
            container.set(TIME_LEFT, PersistentDataType.INTEGER, instance.getTimeLeft());
            container.set(SHOW_BOSS_BAR, PersistentDataType.BOOLEAN, instance.showBossBar());
            UUID cause = instance.getCause();
            if (cause != null) {
                container.set(CAUSE, WbsPersistentDataType.UUID, cause);
            }

            return container;
        }

        @Override
        public @NotNull StatusEffectInstance fromPrimitive(@NotNull PersistentDataContainer container, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
            NamespacedKey typeKey = container.get(EFFECT_TYPE, WbsPersistentDataType.NAMESPACED_KEY);
            int initialTime = container.get(INITIAL_TIME, PersistentDataType.INTEGER);
            int timeLeft = container.get(TIME_LEFT, PersistentDataType.INTEGER);
            boolean showBossBar = container.get(SHOW_BOSS_BAR, PersistentDataType.BOOLEAN);
            UUID cause = container.get(CAUSE, WbsPersistentDataType.UUID);

            StatusEffectInstance instance = new StatusEffectInstance(WandcraftRegistries.STATUS_EFFECTS.get(typeKey), initialTime, showBossBar, cause);
            instance.setTimeLeft(timeLeft);

            return instance;
        }
    }

    public static class PersistentWandType implements PersistentDataType<PersistentDataContainer, Wand> {
        private static final NamespacedKey INVENTORY = WbsWandcraft.getKey("inventory");
        private static final NamespacedKey INVENTORY_TYPE = WbsWandcraft.getKey("type");
        private static final NamespacedKey WAND_ATTRIBUTES = WbsWandcraft.getKey("wand_attributes");
        private static final NamespacedKey WAND_ATTRIBUTE_MODIFIERS = WbsWandcraft.getKey("wand_attribute_modifiers");
        private static final NamespacedKey UUID = WbsWandcraft.getKey("wand_uuid");

        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<Wand> getComplexType() {
            return Wand.class;
        }

        @Override
        public @NotNull PersistentDataContainer toPrimitive(@NotNull Wand wand, @NotNull PersistentDataAdapterContext context) {
            PersistentDataContainer container = context.newPersistentDataContainer();

            wand.writeAttributes(container, WAND_ATTRIBUTES);

            List<PersistentDataContainer> modifierContainerList = new LinkedList<>();
            for (SpellAttributeModifier<?, ?> attributeModifier : wand.getAttributeModifiers()) {
                PersistentDataContainer modifierContainer = context.newPersistentDataContainer();
                attributeModifier.writeTo(modifierContainer);
                modifierContainerList.add(modifierContainer);
            }
            container.set(WAND_ATTRIBUTE_MODIFIERS, PersistentDataType.LIST.dataContainers(), modifierContainerList);
            container.set(UUID, PersistentDataType.STRING, wand.getUUID());

            PersistentDataContainer itemsContainer = context.newPersistentDataContainer();
            wand.getItems().rowMap().forEach((row, columnMap) -> {
                PersistentDataContainer columnContainer = context.newPersistentDataContainer();

                columnMap.forEach((column, item) -> {
                    columnContainer.set(WbsWandcraft.getKey(column.toString()), WbsPersistentDataType.ITEM_AS_BYTES, item);
                });

                itemsContainer.set(WbsWandcraft.getKey(row.toString()), PersistentDataType.TAG_CONTAINER, columnContainer);
            });

            container.set(INVENTORY, PersistentDataType.TAG_CONTAINER, itemsContainer);
            container.set(INVENTORY_TYPE, WbsPersistentDataType.NAMESPACED_KEY, wand.getInventoryType().getKey());

            return container;
        }

        @Override
        public @NotNull Wand fromPrimitive(@NotNull PersistentDataContainer container, @NotNull PersistentDataAdapterContext context) {
            NamespacedKey typeKey = container.get(INVENTORY_TYPE, WbsPersistentDataType.NAMESPACED_KEY);
            if (typeKey == null) {
                throw new IllegalStateException("inventory type (typeKey) missing!");
            }

            WandInventoryType type = WandcraftRegistries.WAND_INVENTORY_TYPES.get(typeKey);

            if (type == null) {
                throw new IllegalStateException("Wand inventory type missing for key " + typeKey.asString());
            }

            String uuid = container.get(UUID, PersistentDataType.STRING);

            Wand wand = new Wand(type, Objects.requireNonNull(uuid));

            wand.readAttributes(container, WAND_ATTRIBUTES);

            List<PersistentDataContainer> modifierContainerList = container.get(WAND_ATTRIBUTE_MODIFIERS, PersistentDataType.LIST.dataContainers());
            if (modifierContainerList != null) {
                for (PersistentDataContainer modifierContainer : modifierContainerList) {
                    SpellAttributeModifier<?, ?> attributeModifier = SpellAttributeModifier.fromContainer(modifierContainer);

                    wand.setModifier(attributeModifier);
                }
            }

            PersistentDataContainer itemsContainer = container.get(INVENTORY, PersistentDataType.TAG_CONTAINER);
            if (itemsContainer != null) {
                for (NamespacedKey rowKey : itemsContainer.getKeys()) {
                    PersistentDataContainer rowContainer = itemsContainer.get(rowKey, PersistentDataType.TAG_CONTAINER);

                    if (rowContainer != null) {
                        for (NamespacedKey columnKey : rowContainer.getKeys()) {
                            ItemStack item = rowContainer.get(columnKey, WbsPersistentDataType.ITEM_AS_BYTES);

                            if (item != null) {
                                Integer row = Integer.valueOf(rowKey.value());
                                Integer column = Integer.valueOf(columnKey.value());

                                wand.getItems().put(row, column, item);
                            }
                        }
                    }
                }

                container.set(INVENTORY, PersistentDataType.TAG_CONTAINER, itemsContainer);
            }

            return wand;
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

            spellInstance.writeAttributes(container, ATTRIBUTES);
            container.set(DEFINITION, WbsPersistentDataType.NAMESPACED_KEY, spellInstance.getDefinition().getKey());

            return container;
        }

        @Override
        public @NotNull SpellInstance fromPrimitive(@NotNull PersistentDataContainer container, @NotNull PersistentDataAdapterContext context) {
            NamespacedKey definitionKey = container.get(DEFINITION, WbsPersistentDataType.NAMESPACED_KEY);
            if (definitionKey == null) {
                throw new IllegalStateException("Spell definition key missing!");
            }

            SpellDefinition definition = WandcraftRegistries.SPELLS.get(definitionKey);
            if (definition == null) {
                throw new IllegalStateException("Spell definition not recognised:" + definitionKey.asString());
            }

            SpellInstance spellInstance = new SpellInstance(definition);
            spellInstance.readAttributes(container, ATTRIBUTES);

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
            for (SpellAttributeModifier<?, ?> attributeModifier : modifier.getModifiers()) {
                PersistentDataContainer modifierContainer = context.newPersistentDataContainer();
                attributeModifier.writeTo(modifierContainer);
                modifierContainerList.add(modifierContainer);
            }
            container.set(ATTRIBUTE_MODIFIERS, PersistentDataType.LIST.dataContainers(), modifierContainerList);

            List<PersistentDataContainer> effectContainerList = new LinkedList<>();
            for (SpellEffectInstance<?> effect : modifier.getEffects()) {
                PersistentDataContainer effectContainer = context.newPersistentDataContainer();

                effectContainer.set(DEFINITION, WbsPersistentDataType.NAMESPACED_KEY, effect.getDefinition().getKey());

                effect.writeAttributes(container, EFFECT_ATTRIBUTES);

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
                SpellAttributeModifier<?, ?> attributeModifier = SpellAttributeModifier.fromContainer(modifierContainer);

                spellModifier.addModifier(attributeModifier);
            }

            List<PersistentDataContainer> effectContainerList = container.get(EFFECTS, PersistentDataType.LIST.dataContainers());
            if (effectContainerList == null) {
                throw new IllegalStateException("Deserialization of " + EFFECTS.asString() + " missing!");
            }

            for (PersistentDataContainer effectContainer : effectContainerList) {
                NamespacedKey definitionKey = effectContainer.get(DEFINITION, WbsPersistentDataType.NAMESPACED_KEY);
                SpellEffectDefinition<?> definition = WandcraftRegistries.EFFECTS.get(definitionKey);

                if (definition == null) {
                    throw new IllegalStateException("Effect definition not found for key " + definitionKey);
                }

                SpellEffectInstance<?> effectInstance = new SpellEffectInstance<>(definition);

                PersistentDataContainer attributesContainer = effectContainer.get(EFFECT_ATTRIBUTES, PersistentDataType.TAG_CONTAINER);
                if (attributesContainer != null) {
                    for (NamespacedKey attributeKey : attributesContainer.getKeys()) {
                        SpellAttribute<?> attribute = Objects.requireNonNull(
                                WandcraftRegistries.ATTRIBUTES.get(attributeKey),
                                "Attribute key was not present in registry! This is a bug. " + attributeKey.asString()
                        );
                        SpellAttributeInstance<?> instance = attribute.getInstance(attributesContainer);

                        effectInstance.setAttribute(instance);
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
        public @NotNull SpellAttributeModifier<?, ?> fromPrimitive(@NotNull PersistentDataContainer container, @NotNull PersistentDataAdapterContext context) {
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
