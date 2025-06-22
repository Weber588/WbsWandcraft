package wbs.wandcraft.spell.event;

import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.context.CastContext;

import java.util.function.Consumer;

@NullMarked
public class SpellTriggeredEvent<T> implements Keyed {
    private final NamespacedKey key;
    private final Class<T> eventClass;

    public SpellTriggeredEvent(NamespacedKey key, Class<T> eventClass) {
        this.key = key;
        this.eventClass = eventClass;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public Class<T> getEventClass() {
        return eventClass;
    }

    public SpellEffectInstance<T> getAnonymousInstance(TriConsumer<CastContext, SpellEffectInstance<T>, T> consumer) {
        SpellEffectDefinition<T> definition = SpellEffectDefinition.anonymous(getEventClass(), consumer);

        return new SpellEffectInstance<>(definition);
    }

    public void registerAnonymous(SpellInstance instance, Consumer<T> eventConsumer) {
        instance.registerEffect(
                getAnonymousInstance((context, effect, result) ->
                        eventConsumer.accept(result)
                )
        );
    }

    public SupportedEvent<T, T> getSupportedEvent() {
        return new SupportedEvent<>(getEventClass(), a -> a);
    }
}
