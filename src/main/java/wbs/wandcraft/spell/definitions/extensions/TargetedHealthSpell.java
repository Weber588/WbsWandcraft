package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.entity.LivingEntity;
import wbs.wandcraft.context.CastContext;

import java.util.List;
import java.util.function.Consumer;

public interface TargetedHealthSpell extends HealthSpell, TargetedSpell<LivingEntity> {
    default void healTargets(CastContext context) {
        healTargets(context, target -> {}, target -> {});
    }
    default void healTargetsWithParticles(CastContext context) {
        List<LivingEntity> targets = getTargets(context);

        for (LivingEntity target : targets) {
            healWithParticles(context, target);
        }
    }
    default void healTargets(CastContext context, Consumer<LivingEntity> onHeal, Consumer<LivingEntity> onDamage) {
        List<LivingEntity> targets = getTargets(context);

        for (LivingEntity target : targets) {
            heal(context, target, () -> onHeal.accept(target), () -> onDamage.accept(target));
        }
    }
}
