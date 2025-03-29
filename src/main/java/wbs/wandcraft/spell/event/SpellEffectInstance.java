package wbs.wandcraft.spell.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import wbs.utils.util.WbsKeyed;
import wbs.wandcraft.ComponentRepresentable;
import wbs.wandcraft.spell.attributes.Attributable;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.definitions.extensions.CastContext;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class SpellEffectInstance<T> implements Attributable, ComponentRepresentable {
    private final SpellEffectDefinition<T> definition;
    private final Set<SpellAttributeInstance<?>> attributeValues = new HashSet<>();
    private final Set<SpellTriggeredEvent<?>> allowedEvents = new HashSet<>();

    public SpellEffectInstance(SpellEffectDefinition<T> definition) {
        this.definition = definition;

        definition.getAttributeValues().forEach(this::addAttribute);
    }

    public SpellEffectDefinition<T> getDefinition() {
        return definition;
    }

    public <O> boolean run(CastContext context, SpellTriggeredEvent<O> trigger, O event) {
        Function<O, T> mapper = definition.getSupportFor(trigger);
        if (mapper == null) {
            return false;
        }

        if (!allowsEvent(trigger)) {
            return false;
        }

        definition.run(context, this, mapper.apply(event));
        return true;
    }

    @Override
    public Component toComponent() {
        Component asComponent = definition.toComponent().color(NamedTextColor.GOLD);

        if (!attributeValues.isEmpty()) {
            asComponent = asComponent.appendNewline().append(Component.text("  Attributes::").color(NamedTextColor.AQUA));
            for (SpellAttributeInstance<?> instance : attributeValues) {
                if (instance.shouldShow()) {
                    asComponent = asComponent.appendNewline().append(Component.text("    > ")).append(instance.toComponent());
                }
            }
        }

        if (!allowedEvents.isEmpty()) {
            asComponent = asComponent.appendNewline().append(Component.text("  Can Affect:").color(NamedTextColor.AQUA));
            for (SpellTriggeredEvent<?> trigger : allowedEvents) {
                asComponent = asComponent.appendNewline().append(Component.text("    > " + WbsKeyed.toPrettyString(trigger)));
            }
        }

        return asComponent;
    }

    @Override
    public Set<SpellAttributeInstance<?>> getAttributeValues() {
        return attributeValues;
    }
    
    public void addAllowedTrigger(SpellTriggeredEvent<?> trigger) {
        allowedEvents.add(trigger);
    }

    public Set<SpellTriggeredEvent<?>> getAllowedEvents() {
        return new HashSet<>(allowedEvents);
    }

    public boolean allowsEvent(SpellTriggeredEvent<?> trigger) {
        return allowedEvents.isEmpty() || allowedEvents.contains(trigger);
    }
}
