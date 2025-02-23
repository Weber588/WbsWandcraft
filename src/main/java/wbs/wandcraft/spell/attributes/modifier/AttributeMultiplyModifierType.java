package wbs.wandcraft.spell.attributes.modifier;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.WbsWandcraft;

public class AttributeMultiplyModifierType implements AttributeModifierType {
    @SuppressWarnings("unchecked")
    @Override
    public <T> T modify(T current, T value) {

        switch (current) {
            case Integer integer -> {
                return (T) Integer.valueOf(integer * (Integer) value);
            }
            case Number number -> {
                return (T) Double.valueOf(number.doubleValue() * ((Number) value).doubleValue());
            }
            default -> throw new IllegalArgumentException("Multiplication only supports numeric attributes.");
        }
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return WbsWandcraft.getKey("multiply");
    }
}
