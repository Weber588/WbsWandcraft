package wbs.wandcraft.spell.attributes;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

@SuppressWarnings("unused")
public class IntegerSpellAttribute extends SpellAttribute<Integer> {
    public IntegerSpellAttribute(NamespacedKey key, int defaultValue) {
        super(key, PersistentDataType.INTEGER, IntegerArgumentType.integer(), defaultValue, Integer::parseInt);
    }

    public IntegerSpellAttribute(String nativeKey, int defaultValue) {
        super(nativeKey, PersistentDataType.INTEGER, IntegerArgumentType.integer(), defaultValue, Integer::parseInt);
    }

    public IntegerSpellAttribute(String nativeKey, int minValue, int defaultValue) {
        super(nativeKey, PersistentDataType.INTEGER, IntegerArgumentType.integer(minValue), defaultValue, Integer::parseInt);
    }

    public IntegerSpellAttribute(String nativeKey, int minValue, int maxValue, int defaultValue) {
        super(nativeKey, PersistentDataType.INTEGER, IntegerArgumentType.integer(minValue, maxValue), defaultValue, Integer::parseInt);
    }
}
