package wbs.wandcraft.spell.attributes;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.AttributeDataType;

@NullMarked
public class IntegerSpellAttribute extends SpellAttribute<Integer> {
    @SuppressWarnings("unused")
    public IntegerSpellAttribute(NamespacedKey key, int defaultValue) {
        super(key, AttributeDataType.INTEGER, IntegerArgumentType.integer(), defaultValue, Integer::parseInt);
    }

    public IntegerSpellAttribute(String nativeKey, int defaultValue) {
        super(nativeKey, AttributeDataType.INTEGER, IntegerArgumentType.integer(), defaultValue, Integer::parseInt);
    }

    @Override
    public Sentiment getSentiment(Integer value) {
        return value < 0 ? sentiment().invert() : sentiment();
    }
}
