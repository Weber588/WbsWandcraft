package wbs.wandcraft.spell.attributes;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

@SuppressWarnings("unused")
public class DoubleSpellAttribute extends SpellAttribute<Double> {
    public DoubleSpellAttribute(NamespacedKey key, double defaultValue) {
        super(key, PersistentDataType.DOUBLE, DoubleArgumentType.doubleArg(), defaultValue);
    }

    public DoubleSpellAttribute(String nativeKey, double defaultValue) {
        super(nativeKey, PersistentDataType.DOUBLE, DoubleArgumentType.doubleArg(), defaultValue);
    }

    public DoubleSpellAttribute(String nativeKey, double minValue, double defaultValue) {
        super(nativeKey, PersistentDataType.DOUBLE, DoubleArgumentType.doubleArg(minValue), defaultValue);
    }

    public DoubleSpellAttribute(String nativeKey, double minValue, double maxValue, double defaultValue) {
        super(nativeKey, PersistentDataType.DOUBLE, DoubleArgumentType.doubleArg(minValue, maxValue), defaultValue);
    }
}
