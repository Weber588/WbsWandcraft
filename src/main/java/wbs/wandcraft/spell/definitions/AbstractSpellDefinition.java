package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.spell.attributes.SpellAttribute;

import java.util.Collection;

public interface AbstractSpellDefinition extends Keyed {
    void addAttribute(SpellAttribute<?> attribute);

    @Nullable
    <T> SpellAttribute<T> getAttribute(Key key, Class<T> clazz);

    Collection<SpellAttribute<?>> getAttributes();
}
