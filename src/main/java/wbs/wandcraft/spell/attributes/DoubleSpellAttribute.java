package wbs.wandcraft.spell.attributes;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.RegisteredPersistentDataType;

import java.text.DecimalFormat;

@NullMarked
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

    @Override
    public Polarity getPolarity(@NotNull Double value) {
        if (value > 1) {
            return polarity();
        } else {
            return polarity().invert();
        }
    }
}
