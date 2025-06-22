package wbs.wandcraft.spell.event;

import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.wandcraft.ComponentRepresentable;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.Attributable;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.context.CastContext;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@NullMarked
public abstract class SpellEffectDefinition<T> implements Keyed, ComponentRepresentable, Attributable {
    public static <T> SpellEffectDefinition<T> anonymous(Class<T> eventClass, TriConsumer<CastContext, SpellEffectInstance<T>, T> consumer) {
        return new SpellEffectDefinition<>(eventClass, WbsWandcraft.getKey("anonymous")) {
            @Override
            public void run(CastContext context, SpellEffectInstance<T> effectInstance, T event) {
                consumer.accept(context, effectInstance, event);
            }

            @Override
            public Component toComponent() {
                return Component.text("Anonymous");
            }
        };
    }

    private final Class<T> eventClass;

    private final NamespacedKey key;
    private final Set<SpellAttributeInstance<?>> attributeValues = new HashSet<>();
    protected final Set<SupportedEvent<T, ?>> supportedEvents = new HashSet<>();

    public SpellEffectDefinition(Class<T> eventClass, NamespacedKey key) {
        this.eventClass = eventClass;
        this.key = key;

        supportedEvents.add(new SupportedEvent<>(eventClass, a -> a));
    }

    SpellEffectDefinition(Class<T> eventClass, String keyString) {
        this(eventClass, WbsWandcraft.getKey(keyString));
    }

    public Class<T> getEventClass() {
        return eventClass;
    }

    @Nullable
    public <O> Function<O, T> getSupportFor(SpellTriggeredEvent<O> event) {
        //noinspection unchecked
        return (Function<O, T>) supportedEvents.stream()
                .filter(support -> support.eventClass().equals(event.getEventClass()))
                .findFirst()
                .map(SupportedEvent::function)
                .orElse(null);
    }

    public void run(CastContext context, SpellEffectInstance<T> effectInstance, Supplier<T> event) {
        run(context, effectInstance, event.get());
    }
    public abstract void run(CastContext context, SpellEffectInstance<T> effectInstance, T event);

    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public Set<SpellAttributeInstance<?>> getAttributeValues() {
        return attributeValues;
    }
}
