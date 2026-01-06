package wbs.wandcraft.spell.definitions.extensions;

import net.kyori.adventure.util.Ticks;
import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

public interface ForceSpell extends ISpellDefinition {
    SpellAttribute<Double> FORCE = new DoubleSpellAttribute("force", 1)
            .setShowAttribute(value -> value != 0)
            .overrideTextureValue("speed")
            .setNumericFormatter(Ticks.TICKS_PER_SECOND, speed -> speed + " blocks/second");

    default void setupForce() {
        addAttribute(FORCE);
    }
}
