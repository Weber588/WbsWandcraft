package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.entity.Player;
import wbs.wandcraft.spell.definitions.SpellInstance;

public record CastContext(Player player, SpellInstance instance) {
}
