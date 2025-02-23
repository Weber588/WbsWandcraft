package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.persistence.PersistentDataType;
import wbs.wandcraft.spell.definitions.AbstractSpellDefinition;
import wbs.wandcraft.spell.attributes.SpellAttribute;

public interface RangedSpell extends AbstractSpellDefinition {
    SpellAttribute<Double> RANGE = new SpellAttribute<>("range", PersistentDataType.DOUBLE, 5.0);

    default void setupRanged() {
        addAttribute(RANGE);
    }
}
