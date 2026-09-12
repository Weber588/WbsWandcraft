package wbs.wandcraft.spell.attributes;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.AttributeDataType;

import java.text.DecimalFormat;

@NullMarked
public class DoubleSpellAttribute extends SpellAttribute<Double> {
    private static final DecimalFormat FORMAT = new DecimalFormat("##.##");

    @SuppressWarnings("unused")
    public DoubleSpellAttribute(NamespacedKey key, double defaultValue) {
        super(key, AttributeDataType.DOUBLE, DoubleArgumentType.doubleArg(), defaultValue, Double::parseDouble);
        setFormatter(FORMAT::format);
    }

    public DoubleSpellAttribute(String nativeKey, double defaultValue) {
        super(nativeKey, AttributeDataType.DOUBLE, DoubleArgumentType.doubleArg(), defaultValue, Double::parseDouble);
        setFormatter(FORMAT::format);
    }

    @Override
    public Sentiment getSentiment(@NotNull Double value) {
        if (value > 1) {
            return sentiment();
        } else {
            return sentiment().invert();
        }
    }
}
