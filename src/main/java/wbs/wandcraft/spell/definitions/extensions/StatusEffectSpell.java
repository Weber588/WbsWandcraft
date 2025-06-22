package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.effects.StatusEffectInstance;
import wbs.wandcraft.spell.definitions.SpellInstance;

public interface StatusEffectSpell<T extends LivingEntity> extends CastableSpell, DurationalSpell, TargetedSpell<T> {
    @NotNull StatusEffect getStatusEffect();

    @Override
    default void cast(CastContext context) {
        Player player = context.player();
        SpellInstance instance = context.instance();

        getTargets(context).forEach(target -> StatusEffectInstance.applyEffect(
                target,
                getStatusEffect(),
                instance.getAttribute(DURATION),
                true,
                player
        ));
    }
}
