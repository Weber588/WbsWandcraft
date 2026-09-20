package wbs.wandcraft.spell.effect;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.attributes.attributable.RangeAttributable;
import wbs.wandcraft.spell.trigger.SupportedEvent;
import wbs.wandcraft.util.EntityUtil;

@NullMarked
public class RandomTeleportEffect extends SpellEffectDefinition<LivingEntity> implements RangeAttributable {
    public RandomTeleportEffect() {
        super(LivingEntity.class, "random_teleport");

        setAttribute(RANGE, 8d);

        supportedEvents.add(SupportedEvent.entityFromRaytraceEvent(LivingEntity.class));
    }

    @Override
    public void run(CastContext context, SpellEffectInstance<LivingEntity> effectInstance, LivingEntity entity) {
        EntityUtil.tryRandomTeleport(entity, effectInstance.getAttribute(RANGE));
    }

    @Override
    public Component toComponent(SpellEffectInstance<LivingEntity> instance) {
        return Component.text("Randomly teleport within " + instance.getAttribute(RANGE) + " blocks.");
    }
}
