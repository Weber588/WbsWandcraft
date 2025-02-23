package wbs.wandcraft.spell.modifier;

import org.bukkit.NamespacedKey;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.WandEntry;
import wbs.wandcraft.spell.event.SpellEffectInstance;

import java.util.LinkedList;
import java.util.List;

public class SpellModifier implements WandEntry {
    public static final NamespacedKey SPELL_MODIFIER_KEY = WbsWandcraft.getKey("spell_modifier");

    private final ModifierScope scope;

    private final List<SpellAttributeModifier<?>> modifiers = new LinkedList<>();
    private final List<SpellEffectInstance<?>> effects = new LinkedList<>();

    public SpellModifier(ModifierScope scope) {
        this.scope = scope;
    }

    public void modify(SpellInstance instance) {
        modifiers.forEach(instance::applyModifier);
        effects.forEach(instance::registerEffect);
    }

    public List<SpellAttributeModifier<?>> getModifiers() {
        return new LinkedList<>(modifiers);
    }

    public List<SpellEffectInstance<?>> getEffects() {
        return new LinkedList<>(effects);
    }

    public SpellModifier setModifiers(List<SpellAttributeModifier<?>> modifiers) {
        this.modifiers.clear();
        this.modifiers.addAll(modifiers);
        return this;
    }

    public SpellModifier setEffects(List<SpellEffectInstance<?>> effects) {
        this.effects.clear();
        this.effects.addAll(effects);
        return this;
    }

    public SpellModifier addModifier(SpellAttributeModifier<?> modifier) {
        this.modifiers.add(modifier);
        return this;
    }

    public SpellModifier addEffect(SpellEffectInstance<?> effect) {
        this.effects.add(effect);
        return this;
    }

    public ModifierScope getScope() {
        return scope;
    }
}
