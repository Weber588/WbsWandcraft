package wbs.wandcraft.spell.attributes;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.commands.brigadier.argument.WbsKeyedArgumentType;
import wbs.wandcraft.AttributeDataType;
import wbs.wandcraft.WbsWandcraft;

import java.util.function.Function;

public class KeyedSpellAttribute<T extends Keyed> extends SpellAttribute<T> {
    @SuppressWarnings("unused")
    public KeyedSpellAttribute(NamespacedKey key, @Nullable T defaultValue, AttributeDataType<T> type, String typeName, Function<NamespacedKey, @Nullable T> function) {
        super(key,
                type,
                new WbsKeyedArgumentType<>(WbsWandcraft.getInstance(), typeName, function),
                defaultValue,
                str -> parse(str, function)
        );
        setFormatter(val -> "\"" + val.getKey().asString() + "\"");
        setRawFormatter(val -> val.getKey().asString());
        sentiment(Sentiment.NEUTRAL);
    }

    public KeyedSpellAttribute(String nativeKey, @Nullable T defaultValue, AttributeDataType<T> type, String typeName, Function<NamespacedKey, @Nullable T> function) {
        super(WbsWandcraft.getKey(nativeKey),
                type,
                new WbsKeyedArgumentType<>(WbsWandcraft.getInstance(), typeName, function),
                defaultValue,
                str -> parse(str, function)
        );
        setFormatter(val -> "\"" + val.getKey().asString() + "\"");
        setRawFormatter(val -> val.getKey().asString());
        sentiment(Sentiment.NEUTRAL);
    }

    private static <T extends Keyed> T parse(String string, Function<NamespacedKey, T> function) {
        string = string.replace("\"", "");
        NamespacedKey asKey = NamespacedKey.fromString(string);
        if (asKey == null) return null;
        return function.apply(asKey);
    }
}
