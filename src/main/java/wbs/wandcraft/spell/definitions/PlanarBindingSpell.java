package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.spell.definitions.extensions.StatusEffectAOESpell;

public class PlanarBindingSpell extends SpellDefinition implements StatusEffectAOESpell {
    public PlanarBindingSpell() {
        super("planar_binding");

        setAttribute(DURATION, 100);
    }

    @Override
    public Component description() {
        return Component.text("Prevents all nearby entities from teleporting for a short duration.");
    }

    @Override
    public @NotNull StatusEffect getStatusEffect() {
        return StatusEffect.PLANAR_BINDING;
    }
}
