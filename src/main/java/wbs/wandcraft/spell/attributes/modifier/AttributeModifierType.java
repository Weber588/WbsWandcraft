package wbs.wandcraft.spell.attributes.modifier;

import org.bukkit.Keyed;

public interface AttributeModifierType extends Keyed {
    <T> T modify(T current, T value);
}
