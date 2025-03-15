package wbs.wandcraft.spell.attributes.modifier;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.SpellAttribute;

public class AttributeSetModifierType implements AttributeModifierType {
    @Override
    public <T> T modify(T current, T value) {
        return value;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return WbsWandcraft.getKey("set");
    }

    @Override
    public <T> Component asComponent(SpellAttribute<T> attribute, T modifierValue) {
        return attribute.displayName().append(Component.text(" = " + modifierValue.toString()));
    }
}
