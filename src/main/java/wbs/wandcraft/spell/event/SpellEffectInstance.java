package wbs.wandcraft.spell.event;

import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.definitions.SpellInstance;

import java.util.HashSet;
import java.util.Set;

public class SpellEffectInstance<T> {

    private final SpellEffectDefinition<T> definition;
    private final Set<SpellAttributeInstance<?>> attributeValues = new HashSet<>();

    public SpellEffectInstance(SpellEffectDefinition<T> definition) {
        this.definition = definition;
    }

    public SpellEffectDefinition<T> getDefinition() {
        return definition;
    }

    public Set<SpellAttributeInstance<?>> getAttributes() {
        return attributeValues;
    }

    public void run(SpellInstance instance, T result) {
        definition.run(instance, this, result);
    }

    public void addAttribute(SpellAttributeInstance<?> instance) {
        this.attributeValues.add(instance);
    }
}
