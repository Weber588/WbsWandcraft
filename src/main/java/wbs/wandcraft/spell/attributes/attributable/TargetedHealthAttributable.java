package wbs.wandcraft.spell.attributes.attributable;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.context.CastContext;

import java.util.List;
import java.util.function.Consumer;

public interface TargetedHealthAttributable extends HealthAttributable, TargetAttributable<LivingEntity> {
    default void healTargets(CastContext context) {
        healTargets(context, null, null);
    }
    default List<LivingEntity> healTargetsWithParticles(CastContext context) {
        return applyToTargets(context, target -> healWithParticles(context, target, context.instance().getAttribute(HEALTH)));
    }
    default List<LivingEntity> healTargets(CastContext context, @Nullable Consumer<LivingEntity> onHeal, @Nullable Consumer<LivingEntity> onDamage) {
        return applyToTargets(
                context,
                target -> heal(
                        context,
                        target,
                        onHeal == null ? null : () -> onHeal.accept(target),
                        onDamage == null ? null : () -> onDamage.accept(target)
                )
        );
    }
}
