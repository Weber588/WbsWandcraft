package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.persistence.PersistentDataType;
import wbs.wandcraft.spell.definitions.AbstractSpellDefinition;
import wbs.wandcraft.spell.attributes.SpellAttribute;

public interface DamageSpell extends AbstractSpellDefinition {
    SpellAttribute<Double> DAMAGE = new SpellAttribute<>("damage", PersistentDataType.DOUBLE, 1.0);

    default void setUpDamage() {
        addAttribute(DAMAGE);
    }
}
