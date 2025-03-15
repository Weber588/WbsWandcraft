package wbs.wandcraft.spell.attributes;

import com.mojang.brigadier.arguments.LongArgumentType;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

@SuppressWarnings("unused")
public class LongSpellAttribute extends SpellAttribute<Long> {
    public LongSpellAttribute(NamespacedKey key, long defaultValue) {
        super(key, PersistentDataType.LONG, LongArgumentType.longArg(), defaultValue, Long::parseLong);
    }

    public LongSpellAttribute(String nativeKey, long defaultValue) {
        super(nativeKey, PersistentDataType.LONG, LongArgumentType.longArg(), defaultValue, Long::parseLong);
    }

    public LongSpellAttribute(String nativeKey, long minValue, long defaultValue) {
        super(nativeKey, PersistentDataType.LONG, LongArgumentType.longArg(minValue), defaultValue, Long::parseLong);
    }

    public LongSpellAttribute(String nativeKey, long minValue, long maxValue, long defaultValue) {
        super(nativeKey, PersistentDataType.LONG, LongArgumentType.longArg(minValue, maxValue), defaultValue, Long::parseLong);
    }
}
