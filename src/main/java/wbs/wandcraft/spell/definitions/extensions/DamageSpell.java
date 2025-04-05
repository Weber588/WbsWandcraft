package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

public interface DamageSpell extends ISpellDefinition {
    SpellAttribute<Double> DAMAGE = new DoubleSpellAttribute("damage", 1.0)
            .addSuggestions(1.0, 2.0, 5.0)
            .setShowAttribute(value -> value > 0);

    default void setUpDamage() {
        addAttribute(DAMAGE);
    }
}
