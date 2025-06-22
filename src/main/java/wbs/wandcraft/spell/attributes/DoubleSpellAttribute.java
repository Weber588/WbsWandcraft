package wbs.wandcraft.spell.attributes;

import org.bukkit.NamespacedKey;
import wbs.wandcraft.RegisteredPersistentDataType;

import java.text.DecimalFormat;

public class DoubleSpellAttribute extends SpellAttribute<Double> {
    private static final DecimalFormat FORMAT = new DecimalFormat("##.##");

    @SuppressWarnings("unused")
    public DoubleSpellAttribute(NamespacedKey key, double defaultValue) {
        super(key, RegisteredPersistentDataType.DOUBLE, defaultValue, Double::parseDouble);
        setFormatter(FORMAT::format);
    }

    public DoubleSpellAttribute(String nativeKey, double defaultValue) {
        super(nativeKey, RegisteredPersistentDataType.DOUBLE, defaultValue, Double::parseDouble);
        setFormatter(FORMAT::format);
    }
}
