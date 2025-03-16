package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import wbs.wandcraft.spell.attributes.SpellAttribute;

import java.util.Collection;

public interface AbstractSpellDefinition extends Keyed {
    void addAttribute(SpellAttribute<?> attribute);

    Collection<SpellAttribute<?>> getAttributes();

    Component displayName();
}
