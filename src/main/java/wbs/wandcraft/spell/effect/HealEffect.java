package wbs.wandcraft.spell.effect;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.attributes.attributable.HealthAttributable;
import wbs.wandcraft.spell.trigger.SupportedEvent;

@NullMarked
public class HealEffect extends SpellEffectDefinition<LivingEntity> implements HealthAttributable {
    public HealEffect() {
        super(LivingEntity.class, "heal");

        supportedEvents.add(SupportedEvent.entityFromRaytraceEvent(LivingEntity.class));
    }

    @Override
    public void run(CastContext context, SpellEffectInstance<LivingEntity> effectInstance, LivingEntity event) {
        healWithParticles(context, event, effectInstance.getAttribute(HEALTH));
    }

    @Override
    public Component toComponent(SpellEffectInstance<LivingEntity> instance) {
        return Component.text("Heals " + instance.getAttribute(HEALTH) / 2 + " hearts.");
    }
}
