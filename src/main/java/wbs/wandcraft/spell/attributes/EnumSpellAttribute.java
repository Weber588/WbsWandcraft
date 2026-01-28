package wbs.wandcraft.spell.attributes;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.WbsEnums;
import wbs.wandcraft.RegisteredPersistentDataType;

public class EnumSpellAttribute<T extends Enum<T>> extends SpellAttribute<T> {
    @SuppressWarnings("unused")
    public EnumSpellAttribute(NamespacedKey key, @Nullable T defaultValue, RegisteredPersistentDataType<T> type, Class<T> enumClass) {
        super(key, type, defaultValue, stringValue -> WbsEnums.getEnumFromString(enumClass, stringValue));
        setFormatter(WbsEnums::toPrettyString);
        sentiment(Sentiment.NEUTRAL);
    }

    public EnumSpellAttribute(String nativeKey, @Nullable T defaultValue, RegisteredPersistentDataType<T> type, Class<T> enumClass) {
        super(nativeKey, type, defaultValue, stringValue -> WbsEnums.getEnumFromString(enumClass, stringValue));
        setFormatter(WbsEnums::toPrettyString);
        sentiment(Sentiment.NEUTRAL);
    }
}
