package wbs.wandcraft.spell.attributes;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

@SuppressWarnings("unused")
public class LongSpellAttribute extends SpellAttribute<Long> {
    public LongSpellAttribute(NamespacedKey key, long defaultValue) {
        super(key, PersistentDataType.LONG, defaultValue, Long::parseLong);
    }

    public LongSpellAttribute(String nativeKey, long defaultValue) {
        super(nativeKey, PersistentDataType.LONG, defaultValue, Long::parseLong);
    }
}
