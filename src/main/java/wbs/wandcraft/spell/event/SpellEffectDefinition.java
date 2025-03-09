package wbs.wandcraft.spell.event;

import net.kyori.adventure.key.Keyed;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.ComponentRepresentable;
import wbs.wandcraft.spell.definitions.SpellInstance;

@NullMarked
public abstract class SpellEffectDefinition<T> implements Keyed, ComponentRepresentable {
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
