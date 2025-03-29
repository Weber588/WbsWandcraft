package wbs.wandcraft.spell.attributes.modifier;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.SpellAttribute;

public class AttributeAddModifierType implements AttributeModifierType {
    public static final NamespacedKey KEY = WbsWandcraft.getKey("add");

    @SuppressWarnings("unchecked")
    @Override
    public <T> T modify(T current, T value) {
        switch (current) {
            case Integer integer -> {
                return (T) Integer.valueOf(integer + (Integer) value);
            }
            case Number number -> {
                return (T) Double.valueOf(number.doubleValue() + ((Number) value).doubleValue());
            }
            default -> throw new IllegalArgumentException("Addition only supports numeric attributes.");
        }
    }

    @Override
    public <T> Component asComponent(SpellAttribute<T> attribute, T modifierValue) {
        return attribute.displayName().append(Component.text(" +" + attribute.formatValue(modifierValue)));
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return KEY;
    }
}
