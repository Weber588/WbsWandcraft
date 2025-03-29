package wbs.wandcraft.spell.attributes;

import org.bukkit.persistence.PersistentDataType;

public class BooleanSpellAttribute extends SpellAttribute<Boolean> {
    public BooleanSpellAttribute(String key, boolean defaultValue) {
        super(key, PersistentDataType.BOOLEAN, defaultValue, Boolean::parseBoolean);

        setSuggestions(true, false);
        // TODO: Make this translatable, if minecraft has something for this in vanilla pack
        setFormatter(value -> value ? "true" : "false");
    }
}
