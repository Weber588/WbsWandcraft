package wbs.wandcraft.spell.attributes;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.text.DecimalFormat;

@SuppressWarnings("unused")
public class DoubleSpellAttribute extends SpellAttribute<Double> {
    private static final DecimalFormat FORMAT = new DecimalFormat("##.##");

    public DoubleSpellAttribute(NamespacedKey key, double defaultValue) {
        super(key, PersistentDataType.DOUBLE, defaultValue, Double::parseDouble);
        setFormatter(FORMAT::format);
    }

    public DoubleSpellAttribute(String nativeKey, double defaultValue) {
        super(nativeKey, PersistentDataType.DOUBLE, defaultValue, Double::parseDouble);
        setFormatter(FORMAT::format);
    }
}
