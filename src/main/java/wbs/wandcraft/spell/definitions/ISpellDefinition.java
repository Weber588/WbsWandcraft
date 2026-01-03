package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import wbs.utils.util.string.WbsStrings;
import wbs.wandcraft.spell.attributes.Attributable;
import wbs.wandcraft.spell.attributes.SpellAttribute;

import java.util.Collection;

public interface ISpellDefinition extends Keyed, Attributable {
    void addAttribute(SpellAttribute<?> attribute);

    Collection<SpellAttribute<?>> getAttributes();

    default String name() {
        return WbsStrings.capitalizeAll(key().value().replaceAll("_", " "));
    }

    Component displayName();
}
