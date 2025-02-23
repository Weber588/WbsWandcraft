package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.persistence.PersistentDataType;
import wbs.wandcraft.spell.definitions.AbstractSpellDefinition;
import wbs.wandcraft.spell.attributes.SpellAttribute;

public interface DurationalSpell extends AbstractSpellDefinition {
    SpellAttribute<Integer> DURATION = new SpellAttribute<>("duration", PersistentDataType.INTEGER, 20);

    default void setUpDurational() {
        addAttribute(DURATION);
    }
}
