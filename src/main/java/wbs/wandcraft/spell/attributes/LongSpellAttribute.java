package wbs.wandcraft.spell.attributes;

import org.bukkit.NamespacedKey;
import wbs.wandcraft.RegisteredPersistentDataType;

public class LongSpellAttribute extends SpellAttribute<Long> {
    @SuppressWarnings("unused")
    public LongSpellAttribute(NamespacedKey key, long defaultValue) {
        super(key, RegisteredPersistentDataType.LONG, defaultValue, Long::parseLong);
    }

    public LongSpellAttribute(String nativeKey, long defaultValue) {
        super(nativeKey, RegisteredPersistentDataType.LONG, defaultValue, Long::parseLong);
    }
}
