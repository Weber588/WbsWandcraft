package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.Ticks;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.spell.definitions.extensions.StatusEffectAOESpell;

public class CharmSpell extends SpellDefinition implements StatusEffectAOESpell {
    public CharmSpell() {
        super("charm");

        setAttribute(DURATION, 15 * Ticks.TICKS_PER_SECOND);
        setAttribute(RADIUS, 5d);
    }

    @Override
    public Component description() {
        return Component.text("Prevents undead from targeting you for the duration of the effect");
    }

    @Override
    public @NotNull StatusEffect getStatusEffect() {
        return StatusEffect.CHARMED;
    }
}
