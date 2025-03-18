package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.spell.definitions.SpellInstance;

@NullMarked
public record CastContext(Player player, SpellInstance instance, Location source, @Nullable CastContext parent) {
    public void cast() {
        instance.cast(this);
    }
    public void cast(SpellInstance other, Location source) {
        other.cast(new CastContext(player, other, source, parent));
    }
}
