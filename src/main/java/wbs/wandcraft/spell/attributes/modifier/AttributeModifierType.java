package wbs.wandcraft.spell.attributes.modifier;

import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import wbs.wandcraft.spell.attributes.SpellAttribute;

public interface AttributeModifierType extends Keyed {
    <T> T modify(T current, T value);

    <T> Component asComponent(SpellAttribute<T> attribute, T modifierValue);
}
