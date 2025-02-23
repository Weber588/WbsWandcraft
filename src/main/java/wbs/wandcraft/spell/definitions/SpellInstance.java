package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.spell.definitions.extensions.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.event.SpellEffectInstance;
import wbs.wandcraft.spell.event.SpellTriggeredEvent;

import java.util.HashSet;
import java.util.Set;

public class SpellInstance implements WandEntry {
    public static final NamespacedKey SPELL_INSTANCE_KEY = WbsWandcraft.getKey("spell_instance");

    private final SpellDefinition definition;
    private final Set<SpellAttributeInstance<?>> attributeValues = new HashSet<>();
    private final Set<SpellEffectInstance<?>> triggeredEffects = new HashSet<>();

    public SpellInstance(SpellDefinition definition) {
        this.definition = definition;

        for (SpellAttribute<?> attribute : definition.getAttributes()) {
            attributeValues.add(attribute.getInstance());
        }
    }

    public void cast(Player player) {
        if (definition instanceof CastableSpell castable) {
            castable.cast(new CastContext(player, this));
        }
    }

    public SpellDefinition getDefinition() {
        return definition;
    }

    public <T> void setAttribute(SpellAttribute<T> attribute, T value) {
        for (SpellAttributeInstance<?> attributeValue : attributeValues) {
            if (attributeValue.attribute().equals(attribute)) {
                attributeValue.value(value);
                return;
            }
        }
    }

    public <T> void setAttribute(Key key, T value) {
        for (SpellAttributeInstance<?> attributeValue : attributeValues) {
            if (attributeValue.attribute().key().equals(key)) {
                attributeValue.value(value);
                return;
            }
        }
    }

    @NotNull
    public <T> T getAttribute(SpellAttribute<T> attribute) {
        //noinspection unchecked
        SpellAttributeInstance<T> instance =
                (SpellAttributeInstance<T>) attributeValues.stream()
                        .filter(value -> value.attribute().equals(attribute))
                        .findFirst()
                        .orElse(null);

        if (instance == null) {
            return attribute.defaultValue();
        }

        return instance.value();
    }

    public Set<SpellAttributeInstance<?>> getAttributes() {
        return new HashSet<>(attributeValues);
    }

    public <T> void setAttribute(SpellAttributeInstance<T> instance) {
        setAttribute(instance.attribute(), instance.value());
    }

    public <T> void applyModifier(SpellAttributeModifier<T> modifier) {
        for (SpellAttributeInstance<?> attributeInstance : getAttributes()) {
            if (attributeInstance.attribute().equals(modifier.attribute())) {
                attributeInstance.modify(modifier);
            }
        }
    }

    public void registerEffect(SpellEffectInstance<?> spellEffect) {
        triggeredEffects.add(spellEffect);
    }

    public <T> Set<SpellEffectInstance<T>> getEffects(SpellTriggeredEvent<T> event) {
        Set<SpellEffectInstance<T>> effects = new HashSet<>();

        for (SpellEffectInstance<?> effect : triggeredEffects) {
            if (effect.getDefinition().getTrigger().equals(event)) {
                //noinspection unchecked
                effects.add((SpellEffectInstance<T>) effect);
            }
        }

        return effects;
    }
}
