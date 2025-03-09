package wbs.wandcraft.spell.event;

import net.kyori.adventure.text.Component;
import wbs.wandcraft.ComponentRepresentable;
import wbs.wandcraft.spell.attributes.Attributable;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.definitions.SpellInstance;

import java.util.HashSet;
import java.util.Set;

public class SpellEffectInstance<T> implements Attributable, ComponentRepresentable {
    private final SpellEffectDefinition<T> definition;
    private final Set<SpellAttributeInstance<?>> attributeValues = new HashSet<>();

    public SpellEffectInstance(SpellEffectDefinition<T> definition) {
        this.definition = definition;
    }

    public SpellEffectDefinition<T> getDefinition() {
        return definition;
    }

    public void run(SpellInstance instance, T result) {
        definition.run(instance, this, result);
    }

    @Override
    public Component toComponent() {
        Component asComponent = definition.toComponent();

        for (SpellAttributeInstance<?> instance : attributeValues) {
            asComponent = asComponent.append(Component.text("\n    ")).append(instance.toComponent());
        }

        return asComponent;
    }

    @Override
    public Set<SpellAttributeInstance<?>> getAttributeValues() {
        return attributeValues;
    }
}
