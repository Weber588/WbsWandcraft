package wbs.wandcraft.spell.effect;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import wbs.utils.util.WbsKeyed;
import wbs.wandcraft.ComponentRepresentable;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.attributes.AttributeHolder;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.trigger.SpellTriggeredEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpellEffectInstance<T> implements AttributeHolder, ComponentRepresentable {
    private final SpellEffectDefinition<T> effect;
    private final Set<SpellAttributeInstance<?>> attributeValues = new HashSet<>();
    private final Set<SpellTriggeredEvent<?>> triggers = new HashSet<>();
    private double chance = 1;

    public SpellEffectInstance(SpellEffectDefinition<T> effect) {
        this.effect = effect;

        effect.getAttributeInstances().forEach(this::setAttribute);
    }

    public SpellEffectDefinition<T> getEffect() {
        return effect;
    }

    public <O> boolean run(CastContext context, SpellTriggeredEvent<O> trigger, O event) {
        if (Math.random() < chance) {
            return false;
        }

        Function<O, T> mapper = effect.getSupportFor(trigger);
        if (mapper == null) {
            return false;
        }

        if (!allowsEvent(trigger)) {
            return false;
        }

        T mapped = mapper.apply(event);
        if (mapped != null) {
            // An event running should never interrupt the outer action
            try {
                effect.run(context, this, mapped);
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public Component toComponent() {
        Component asComponent = Component.text("On ");
        if (!triggers.isEmpty()) {
            String triggersString = triggers
                    .stream()
                    .map(WbsKeyed::toPrettyString)
                    .collect(Collectors.joining(", "));

            asComponent = asComponent.append(
                    Component.text(triggersString + ": ")
            );
        }

        asComponent = asComponent.append(effect.toComponent(this));

        return asComponent;
    }

    @Override
    public Set<SpellAttributeInstance<?>> getAttributeInstances() {
        return attributeValues;
    }
    
    public SpellEffectInstance<T> addTrigger(SpellTriggeredEvent<?> trigger) {
        triggers.add(trigger);
        return this;
    }

    public Set<SpellTriggeredEvent<?>> getTriggers() {
        return new HashSet<>(triggers);
    }

    public boolean allowsEvent(SpellTriggeredEvent<?> trigger) {
        return triggers.isEmpty() || triggers.contains(trigger);
    }

    public double chance() {
        return chance;
    }

    public SpellEffectInstance<T> chance(double chance) {
        this.chance = chance;
        return this;
    }

    @Override
    public String toString() {
        return PlainTextComponentSerializer.plainText().serialize(toComponent());
    }
}
