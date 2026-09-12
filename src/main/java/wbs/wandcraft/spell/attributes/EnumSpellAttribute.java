package wbs.wandcraft.spell.attributes;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.commands.brigadier.argument.WbsEnumArgumentType;
import wbs.wandcraft.AttributeDataType;

public class EnumSpellAttribute<T extends Enum<T>> extends SpellAttribute<T> {
    @SuppressWarnings("unused")
    public EnumSpellAttribute(NamespacedKey key, @Nullable T defaultValue, AttributeDataType<T> type, Class<T> enumClass) {
        super(key, type, new WbsEnumArgumentType<>(enumClass), defaultValue, stringValue -> WbsEnums.getEnumFromString(enumClass, stringValue));
        setFormatter(WbsEnums::toPrettyString);
        setRawFormatter(Enum::name);
        sentiment(Sentiment.NEUTRAL);
    }

    public EnumSpellAttribute(String nativeKey, @Nullable T defaultValue, AttributeDataType<T> type, Class<T> enumClass) {
        super(nativeKey, type, new WbsEnumArgumentType<>(enumClass), defaultValue, stringValue -> WbsEnums.getEnumFromString(enumClass, stringValue));
        setFormatter(WbsEnums::toPrettyString);
        setRawFormatter(Enum::name);
        sentiment(Sentiment.NEUTRAL);
    }
}
