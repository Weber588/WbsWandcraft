package wbs.wandcraft.spell.attributes;

import org.bukkit.NamespacedKey;
import wbs.wandcraft.RegisteredPersistentDataType;

public class IntegerSpellAttribute extends SpellAttribute<Integer> {
    @SuppressWarnings("unused")
    public IntegerSpellAttribute(NamespacedKey key, int defaultValue) {
        super(key, RegisteredPersistentDataType.INTEGER, defaultValue, Integer::parseInt);
    }

    public IntegerSpellAttribute(String nativeKey, int defaultValue) {
        super(nativeKey, RegisteredPersistentDataType.INTEGER, defaultValue, Integer::parseInt);
    }
}
