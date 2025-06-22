package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.spell.definitions.extensions.StatusEffectSelfSpell;

public class DeathWalkSpell extends SpellDefinition implements StatusEffectSelfSpell {
    public DeathWalkSpell() {
        super("death_walk");

        setAttribute(DURATION, 200);
    }

    @Override
    public Component description() {
        return Component.text("Prevents undead from targeting you for the duration of the effect");
    }

    @Override
    public @NotNull StatusEffect getStatusEffect() {
        return StatusEffect.DEATH_WALK;
    }
}
