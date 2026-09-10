package wbs.wandcraft.util.persistent;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import wbs.utils.util.persistent.KeyedPersistentDataType;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.event.SpellEffectDefinition;
import wbs.wandcraft.spell.event.SpellEffectInstance;
import wbs.wandcraft.spell.event.SpellTriggeredEvent;

import java.util.List;

public class PersistentSpellEffectInstanceType implements WbsPersistentDataType<PersistentDataContainer, SpellEffectInstance<?>> {
    private static final NamespacedKey DEFINITION = WbsWandcraft.getKey("definition");
    private static final NamespacedKey TRIGGERS = WbsWandcraft.getKey("triggers");
    private static final NamespacedKey ATTRIBUTES = WbsWandcraft.getKey("attributes");

    @Override
    public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
        return PersistentDataContainer.class;
    }

    @Override
    public @NotNull Class<SpellEffectInstance<?>> getComplexType() {
        //noinspection unchecked
        return (Class<SpellEffectInstance<?>>) (Class<?>) SpellEffectInstance.class;
    }

    @Override
    public @NonNull PersistentDataContainer toPrimitive(@NonNull SpellEffectInstance<?> instance, @NotNull PersistentDataAdapterContext context) {
        PersistentDataContainer container = context.newPersistentDataContainer();

        container.set(DEFINITION, WbsPersistentDataType.NAMESPACED_KEY, instance.getEffect().getKey());

        //noinspection unchecked
        container.set(
                TRIGGERS,
                new KeyedPersistentDataType<>(
                        (Class<SpellTriggeredEvent<?>>) (Class<?>) SpellTriggeredEvent.class,
                        WandcraftRegistries.SPELL_TRIGGERS
                ).asList(),
                instance.getTriggers().stream().toList()
        );

        instance.writeAttributes(container, ATTRIBUTES);

        return container;
    }

    @Override
    public @NonNull SpellEffectInstance<?> fromPrimitive(@NonNull PersistentDataContainer container, @NotNull PersistentDataAdapterContext context) {
        NamespacedKey definitionKey = container.get(DEFINITION, WbsPersistentDataType.NAMESPACED_KEY);
        SpellEffectDefinition<?> definition = WandcraftRegistries.EFFECTS.get(definitionKey);

        if (definition == null) {
            throw new IllegalStateException("Effect definition not found for key " + definitionKey);
        }

        SpellEffectInstance<?> effectInstance = new SpellEffectInstance<>(definition);

        container.getOrDefault(TRIGGERS, WbsPersistentDataType.NAMESPACED_KEY.asList(), List.of())
                .stream()
                .map(WandcraftRegistries.SPELL_TRIGGERS)
                .forEach(effectInstance::addTrigger);

        effectInstance.readAttributes(container, ATTRIBUTES);

        return effectInstance;
    }
}
