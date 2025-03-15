package wbs.wandcraft.spell.event;

import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.ComponentRepresentable;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.definitions.SpellInstance;

@NullMarked
public abstract class SpellEffectDefinition<T> implements Keyed, ComponentRepresentable {
    public static <T> SpellEffectDefinition<T> anonymous(SpellTriggeredEvent<T> trigger, TriConsumer<SpellInstance, SpellEffectInstance<T>, T> consumer) {
        return new SpellEffectDefinition<>(trigger, WbsWandcraft.getKey("anonymous")) {
            @Override
            public void run(SpellInstance instance, SpellEffectInstance<T> effectInstance, T event) {
                consumer.accept(instance, effectInstance, event);
            }

            @Override
            public Component toComponent() {
                return Component.text("Anonymous");
            }
        };
    }

    private final SpellTriggeredEvent<T> trigger;
    private final NamespacedKey key;

    public SpellEffectDefinition(SpellTriggeredEvent<T> trigger, NamespacedKey key) {
        this.trigger = trigger;
        this.key = key;
    }

    public SpellTriggeredEvent<T> getTrigger() {
        return trigger;
    }

    public abstract void run(SpellInstance instance, SpellEffectInstance<T> effectInstance, T event);

    public NamespacedKey getKey() {
        return key;
    }
}
