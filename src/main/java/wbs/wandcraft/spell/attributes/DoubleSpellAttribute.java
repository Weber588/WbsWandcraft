package wbs.wandcraft.spell.attributes;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

@SuppressWarnings("unused")
public class DoubleSpellAttribute extends SpellAttribute<Double> {
    public DoubleSpellAttribute(NamespacedKey key, double defaultValue) {
        super(key, PersistentDataType.DOUBLE, defaultValue, Double::parseDouble);
    }

    public DoubleSpellAttribute(String nativeKey, double defaultValue) {
        super(nativeKey, PersistentDataType.DOUBLE, defaultValue, Double::parseDouble);
    }

    public DoubleSpellAttribute(String nativeKey, double minValue, double defaultValue) {
        super(nativeKey, PersistentDataType.DOUBLE, defaultValue, Double::parseDouble);
    }

    public DoubleSpellAttribute(String nativeKey, double minValue, double maxValue, double defaultValue) {
        super(nativeKey, PersistentDataType.DOUBLE, defaultValue, Double::parseDouble);
    }
}
