package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.AbstractSpellDefinition;

public interface DamageSpell extends AbstractSpellDefinition {
    SpellAttribute<Double> DAMAGE = new DoubleSpellAttribute("damage", 0, 1.0)
            .addSuggestions(1.0, 2.0, 5.0);

    default void setUpDamage() {
        addAttribute(DAMAGE);
    }
}
