package wbs.wandcraft.spell.attributes;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.RegisteredPersistentDataType;

@NullMarked
public class LongSpellAttribute extends SpellAttribute<Long> {
    @SuppressWarnings("unused")
    public LongSpellAttribute(NamespacedKey key, long defaultValue) {
        super(key, RegisteredPersistentDataType.LONG, defaultValue, Long::parseLong);
    }

    public LongSpellAttribute(String nativeKey, long defaultValue) {
        super(nativeKey, RegisteredPersistentDataType.LONG, defaultValue, Long::parseLong);
    }

    @Override
    public Sentiment getSentiment(@NotNull Long value) {
        return value < 0 ? sentiment().invert() : sentiment();
    }

}
