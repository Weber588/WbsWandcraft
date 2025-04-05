package wbs.wandcraft.spell.attributes;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

public class IntegerSpellAttribute extends SpellAttribute<Integer> {
    public IntegerSpellAttribute(NamespacedKey key, int defaultValue) {
        super(key, PersistentDataType.INTEGER, defaultValue, Integer::parseInt);
    }

    public IntegerSpellAttribute(String nativeKey, int defaultValue) {
        super(nativeKey, PersistentDataType.INTEGER, defaultValue, Integer::parseInt);
    }
}
