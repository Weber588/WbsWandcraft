package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.effects.StatusEffectInstance;
import wbs.wandcraft.spell.definitions.SpellInstance;

public interface StatusEffectAOESpell extends CastableSpell, StatusEffectSpell, RadiusedSpell {
    @Override
    default void cast(CastContext context) {
        Player player = context.player();
        SpellInstance instance = context.instance();

        player.getWorld().spawnParticle(Particle.FLASH, context.location(), 0);

        new RadiusSelector<>(LivingEntity.class)
                .setRange(instance.getAttribute(RADIUS))
                .exclude(player)
                .select(context.location()).forEach(
                target -> StatusEffectInstance.applyEffect(
                        target,
                        getStatusEffect(),
                        instance.getAttribute(DURATION),
                        true
                )
        );
    }
}
