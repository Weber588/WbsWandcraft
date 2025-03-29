package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

public interface SpeedSpell extends ISpellDefinition {
    SpellAttribute<Double> SPEED = new DoubleSpellAttribute("speed", 0, 1)
            .setFormatter(speed -> speed / 20 + " blocks/second");

    default void setupSpeed() {
        addAttribute(SPEED);
    }
}
