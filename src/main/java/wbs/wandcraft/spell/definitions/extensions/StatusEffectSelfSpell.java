package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.entity.Player;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.effects.StatusEffectInstance;
import wbs.wandcraft.spell.definitions.SpellInstance;

public interface StatusEffectSelfSpell extends CastableSpell, StatusEffectSpell {
    @Override
    default void cast(CastContext context) {
        Player player = context.player();
        SpellInstance instance = context.instance();

        StatusEffectInstance.applyEffect(
                player,
                getStatusEffect(),
                instance.getAttribute(DURATION),
                true,
                player
        );
    }
}
