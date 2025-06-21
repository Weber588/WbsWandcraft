package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.spell.definitions.extensions.StatusEffectAOESpell;

// TODO: Implement targeting system for these spells that can target one or more entities
public class StunSpell extends SpellDefinition implements StatusEffectAOESpell {
    public StunSpell() {
        super("stun");

        setAttribute(DURATION, 20);
    }

    @Override
    public Component description() {
        return Component.text("Temporarily stuns all mobs in a radius, cancelling eating, drinking, and adding a short cooldown to held items.");
    }

    @Override
    public @NotNull StatusEffect getStatusEffect() {
        return StatusEffect.STUNNED;
    }
}
