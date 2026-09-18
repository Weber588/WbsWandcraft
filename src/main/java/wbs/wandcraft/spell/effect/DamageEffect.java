package wbs.wandcraft.spell.effect;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.attributes.attributable.DamageAttributable;
import wbs.wandcraft.spell.trigger.SupportedEvent;

@NullMarked
public class DamageEffect extends SpellEffectDefinition<LivingEntity> implements DamageAttributable {
    public DamageEffect(Class<LivingEntity> eventClass, String keyString) {
        super(eventClass, keyString);

        supportedEvents.add(SupportedEvent.entityFromRaytraceEvent(LivingEntity.class));
    }

    @Override
    public void run(CastContext context, SpellEffectInstance<LivingEntity> effectInstance, LivingEntity event) {
        damage(context, event);
    }

    @Override
    public Component toComponent(SpellEffectInstance<LivingEntity> instance) {
        return Component.text("Deals " + instance.getAttribute(DAMAGE) + " damage.");
    }
}
