package wbs.wandcraft.spell.definitions.extensions;

import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.effects.StatusEffect;

public interface StatusEffectSpell extends DurationalSpell {
    @NotNull StatusEffect getStatusEffect();
}
