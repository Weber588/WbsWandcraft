package wbs.wandcraft.spell.attributes;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.RegisteredPersistentDataType;

@NullMarked
public class IntegerSpellAttribute extends SpellAttribute<Integer> {
    @SuppressWarnings("unused")
    public IntegerSpellAttribute(NamespacedKey key, int defaultValue) {
        super(key, RegisteredPersistentDataType.INTEGER, defaultValue, Integer::parseInt);
    }

    public IntegerSpellAttribute(String nativeKey, int defaultValue) {
        super(nativeKey, RegisteredPersistentDataType.INTEGER, defaultValue, Integer::parseInt);
    }

    @Override
    public Polarity getPolarity(@NotNull Integer value) {
        return value < 0 ? polarity().invert() : polarity();
    }
}
