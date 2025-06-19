package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

public interface SpeedSpell extends ISpellDefinition {
    SpellAttribute<Double> SPEED = new DoubleSpellAttribute("speed", 1)
            .setShowAttribute(value -> value != 0)
            .setNumericFormatter(20, speed -> speed + " blocks/second");

    default void setupSpeed() {
        addAttribute(SPEED);
    }
}
