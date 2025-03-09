package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;

public interface AbstractProjectileSpell extends CastableSpell {
    SpellAttribute<Double> SPEED = new DoubleSpellAttribute("projectile_speed", 0.001, 0.5);

    default void setupProjectile() {
        addAttribute(SPEED);
    }

}
