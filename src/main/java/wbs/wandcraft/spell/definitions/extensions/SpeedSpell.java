package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.AbstractSpellDefinition;

public interface SpeedSpell extends AbstractSpellDefinition {
    SpellAttribute<Double> SPEED = new DoubleSpellAttribute("speed", 0, 1);

    default void setupSpeed() {
        addAttribute(SPEED);
    }
}
