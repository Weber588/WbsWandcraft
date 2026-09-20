package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.effects.StatusEffectInstance;
import wbs.wandcraft.spell.attributes.attributable.DurationAttributable;
import wbs.wandcraft.spell.attributes.attributable.TargetAttributable;
import wbs.wandcraft.spell.definitions.SpellInstance;

import java.util.List;

public interface StatusEffectSpell<T extends LivingEntity> extends CastableSpell, DurationAttributable, TargetAttributable<T> {
    @NotNull StatusEffect getStatusEffect();

    @Override
    default void cast(CastContext context) {
        Player player = context.player();
        SpellInstance instance = context.instance();

        StatusEffect statusEffect = getStatusEffect();

        List<T> applied = applyToTargets(context, target ->
                StatusEffectInstance.applyEffect(
                        target,
                        statusEffect,
                        instance.getAttribute(DURATION),
                        true,
                        player
                )
        );

        WbsWandcraft.getInstance().getLogger().info("Targeted: " + applied.size());

        if (applied.isEmpty()) {
            WbsWandcraft.getInstance().sendActionBar(getNoTargetsMessage(context), player);
        } else {
            WbsMessageBuilder message = WbsWandcraft.getInstance().buildMessageNoPrefix("Applied ")
                    .append(statusEffect.display())
                    .append(" to ")
                    .append(groupName(applied));

            message.build().send(player);
        }
    }
}
