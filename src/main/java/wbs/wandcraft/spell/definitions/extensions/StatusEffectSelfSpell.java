package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.Particle;
import org.bukkit.entity.Player;
import wbs.wandcraft.effects.StatusEffectInstance;
import wbs.wandcraft.spell.definitions.SpellInstance;

public interface StatusEffectSelfSpell extends CastableSpell, StatusEffectSpell, DurationalSpell {
    @Override
    default void cast(CastContext context) {
        Player player = context.player();
        SpellInstance instance = context.instance();

        player.getWorld().spawnParticle(Particle.FLASH, player.getEyeLocation(), 0);

        StatusEffectInstance.applyEffect(
                player,
                getStatusEffect(),
                instance.getAttribute(DURATION),
                true
        );
    }
}
