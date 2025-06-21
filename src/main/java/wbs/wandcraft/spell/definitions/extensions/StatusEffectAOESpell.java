package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.wandcraft.effects.StatusEffectInstance;
import wbs.wandcraft.spell.definitions.SpellInstance;

public interface StatusEffectAOESpell extends CastableSpell, StatusEffectSpell, RadiusedSpell, DurationalSpell {
    @Override
    default void cast(CastContext context) {
        Player player = context.player();
        SpellInstance instance = context.instance();

        RadiusSelector<LivingEntity> selector = new RadiusSelector<>(LivingEntity.class)
                .setRange(instance.getAttribute(RADIUS));

        player.getWorld().spawnParticle(Particle.FLASH, player.getEyeLocation(), 0);

        selector.selectExcluding(player).forEach(
                target -> StatusEffectInstance.applyEffect(
                        target,
                        getStatusEffect(),
                        instance.getAttribute(DURATION),
                        true
                )
        );
    }
}
