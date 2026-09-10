package wbs.wandcraft.spell.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import wbs.utils.util.WbsKeyed;
import wbs.wandcraft.ComponentRepresentable;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.attributes.Attributable;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SpellEffectInstance<T> implements Attributable, ComponentRepresentable {
    private final SpellEffectDefinition<T> effect;
    private final Set<SpellAttributeInstance<?>> attributeValues = new HashSet<>();
    private final Set<SpellTriggeredEvent<?>> triggers = new HashSet<>();

    public SpellEffectInstance(SpellEffectDefinition<T> effect) {
        this.effect = effect;

        effect.getAttributeInstances().forEach(this::setAttribute);
    }

    public SpellEffectDefinition<T> getEffect() {
        return effect;
    }

    public <O> boolean run(CastContext context, SpellTriggeredEvent<O> trigger, O event) {
        Function<O, T> mapper = effect.getSupportFor(trigger);
        if (mapper == null) {
            return false;
        }

        if (!allowsEvent(trigger)) {
            return false;
        }

        effect.run(context, this, mapper.apply(event));
        return true;
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

    @Override
    public String toString() {
        return PlainTextComponentSerializer.plainText().serialize(toComponent());
    }
}
