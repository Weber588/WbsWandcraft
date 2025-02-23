package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.persistence.PersistentDataType;
import wbs.wandcraft.spell.attributes.SpellAttribute;

public interface AbstractProjectileSpell extends CastableSpell {
    SpellAttribute<Double> SPEED = new SpellAttribute<>("projectile_speed", PersistentDataType.DOUBLE, 0.5);

    default void setupProjectile() {
        addAttribute(SPEED);
    }

}
