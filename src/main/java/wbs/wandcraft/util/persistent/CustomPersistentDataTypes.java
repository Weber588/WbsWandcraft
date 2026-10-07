package wbs.wandcraft.util.persistent;

import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.persistent.KeyedPersistentDataType;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.effects.StatusEffectInstance;
import wbs.wandcraft.spell.MagicDomain;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.dynamic.DynamicSpell;
import wbs.wandcraft.spell.dynamic.SpellArchetype;

import java.util.UUID;
import java.util.function.Function;

public class CustomPersistentDataTypes {
    public static final PersistentSpellEffectInstanceType SPELL_EFFECT = new PersistentSpellEffectInstanceType();
    public static final PersistentSpellModifierType SPELL_MODIFIER = new PersistentSpellModifierType();
    public static final PersistentSpellInstanceType SPELL_INSTANCE = new PersistentSpellInstanceType();
    public static final PersistentDynamicSpellType DYNAMIC_SPELL = new PersistentDynamicSpellType();
    public static final PersistentAttributeModifierType SPELL_ATTRIBUTE_MODIFIER = new PersistentAttributeModifierType();

    public static final PersistentBasicWandType BASIC_WAND_TYPE = new PersistentBasicWandType();
    public static final PersistentApprenticeWandType APPRENTICE_WAND_TYPE = new PersistentApprenticeWandType();
    public static final PersistentWizardryWandType WIZARDRY_WAND_TYPE = new PersistentWizardryWandType();
    public static final PersistentSorceryWandType SORCERY_WAND_TYPE = new PersistentSorceryWandType();
    public static final PersistentMageWandType MAGE_WAND_TYPE = new PersistentMageWandType();
    public static final PersistentWildenWandType WILDEN_WAND_TYPE = new PersistentWildenWandType();
    public static final PersistentBarbarianWandType BARBARIAN_WAND_TYPE = new PersistentBarbarianWandType();
    public static final PersistentBroomstickWandType BROOMSTICK_WAND_TYPE = new PersistentBroomstickWandType();

    public static final PersistentSpellbookType SPELLBOOK_TYPE = new PersistentSpellbookType();

    @SuppressWarnings("Convert2MethodRef") // Registries may be null when this is called; wrap in lambda to lazy reference
    public static final KeyedPersistentDataType<SpellDefinition> CANONICAL_SPELL
            = new KeyedPersistentDataType<>(SpellDefinition.class, key -> WandcraftRegistries.SPELLS.get(key));
    @SuppressWarnings("Convert2MethodRef") // Registries may be null when this is called; wrap in lambda to lazy reference
    public static final KeyedPersistentDataType<MagicDomain> MAGIC_DOMAIN
            = new KeyedPersistentDataType<>(MagicDomain.class, key -> WandcraftRegistries.MAGIC_DOMAINS.get(key));
    @SuppressWarnings("Convert2MethodRef") // Registries may be null when this is called; wrap in lambda to lazy reference
    public static final KeyedPersistentDataType<SpellArchetype> SPELL_ARCHETYPE
            = new KeyedPersistentDataType<>(SpellArchetype.class, key -> WandcraftRegistries.SPELL_ARCHETYPES.get(key));

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

    public static class PersistentDynamicSpellType implements PersistentDataType<PersistentDataContainer, DynamicSpell> {
        private static final NamespacedKey ARCHETYPE = WbsWandcraft.getKey("archetype");
        private static final NamespacedKey PRIMARY_DOMAIN = WbsWandcraft.getKey("primary_magic_domain");
        private static final NamespacedKey SECONDARY_DOMAIN = WbsWandcraft.getKey("secondary_magic_domain");

        @Override
        public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public @NotNull Class<DynamicSpell> getComplexType() {
            return DynamicSpell.class;
        }

        @Override
        public @NonNull PersistentDataContainer toPrimitive(@NonNull DynamicSpell definition, @NotNull PersistentDataAdapterContext context) {
            PersistentDataContainer container = context.newPersistentDataContainer();

            container.set(ARCHETYPE, SPELL_ARCHETYPE, definition.archetype());
            container.set(PRIMARY_DOMAIN, MAGIC_DOMAIN, definition.getPrimaryDomain());
            MagicDomain secondaryDomain = definition.getSecondaryDomain();
            if (secondaryDomain != null) {
                container.set(SECONDARY_DOMAIN, MAGIC_DOMAIN, secondaryDomain);
            }

            return container;
        }

        @Override
        public @NonNull DynamicSpell fromPrimitive(@NonNull PersistentDataContainer container, @NotNull PersistentDataAdapterContext context) {
            SpellArchetype archetype = container.get(ARCHETYPE, SPELL_ARCHETYPE);
            if (archetype == null) {
                throw new IllegalStateException("Spell archetype missing!");
            }
            MagicDomain primaryDomain = container.get(PRIMARY_DOMAIN, MAGIC_DOMAIN);
            MagicDomain secondaryDomain = container.get(SECONDARY_DOMAIN, MAGIC_DOMAIN);

            return archetype.build(primaryDomain, secondaryDomain);
        }
    }

    public static class PersistentSpellInstanceType implements PersistentDataType<PersistentDataContainer, SpellInstance> {
        private static final NamespacedKey ATTRIBUTES = WbsWandcraft.getKey( "attributes");
        private static final NamespacedKey DYNAMIC = WbsWandcraft.getKey( "dynamic");
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

            SpellDefinition definition = spellInstance.getDefinition();

            if (definition instanceof DynamicSpell dynamicSpell) {
                container.set(DEFINITION, DYNAMIC_SPELL, dynamicSpell);
                container.set(DYNAMIC, BOOLEAN, true);
            } else {
                container.set(DEFINITION, CANONICAL_SPELL, definition);
            }

            return container;
        }

        @Override
        public @NotNull SpellInstance fromPrimitive(@NotNull PersistentDataContainer container, @NotNull PersistentDataAdapterContext context) {
            boolean dynamic = container.getOrDefault(DYNAMIC, BOOLEAN, false);

            SpellDefinition definition;
            if (dynamic) {
                definition = container.get(DEFINITION, DYNAMIC_SPELL);
                if (definition == null) {
                    throw new IllegalStateException("Dynamic spell definition missing!");
                }
            } else {
                definition = container.get(DEFINITION, CANONICAL_SPELL);
                if (definition == null) {
                    throw new IllegalStateException("Canonical spell definition missing!");
                }
            }

            SpellInstance spellInstance = definition.newInstance();
            spellInstance.readAttributes(container, ATTRIBUTES);

            return spellInstance;
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

    public static class PersistentKeyedType<T extends Keyed> implements PersistentDataType<String, T> {
        // Force mark T as notnull, even if a nullable function is provided
        public static <T extends Keyed> PersistentKeyedType<@NotNull T> get(Class<T> clazz, Function<Key, T> function) {
            return new PersistentKeyedType<>(clazz, function);
        }

        private final Function<Key, T> function;
        private final Class<T> clazz;

        private PersistentKeyedType(Class<T> clazz, Function<Key, T> function) {
            this.function = function;
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
        public @NotNull String toPrimitive(@NotNull T keyed, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
            return keyed.getKey().asString();
        }

        @Override
        public @NotNull T fromPrimitive(@NotNull String asString, @NotNull PersistentDataAdapterContext persistentDataAdapterContext) {
            NamespacedKey namespacedKey = WbsPersistentDataType.NAMESPACED_KEY.fromPrimitive(asString, persistentDataAdapterContext);

            T found = function.apply(namespacedKey);
            if (found == null) {
                throw new IllegalStateException("Keyed value not found! " + clazz.getCanonicalName() + ": " + asString);
            }

            return found;
        }
    }
}
