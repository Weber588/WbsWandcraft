package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.event.SpellTriggeredEvent;

@NullMarked
public record CastContext(Player player, SpellInstance instance, Location location, @Nullable CastContext parent) {
    public void cast() {
        instance.cast(this);
    }
    public void cast(SpellInstance other, Location source) {
        other.cast(new CastContext(player, other, source, parent));
    }

    public <T> void runEffects(SpellTriggeredEvent<T> trigger, T event) {
        instance.getEffects(trigger).forEach(effect -> effect.run(this, trigger, event));
    }
}
