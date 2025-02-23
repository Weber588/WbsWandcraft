package wbs.wandcraft.spell.attributes.modifier;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.WbsWandcraft;

public class AttributeSetModifierType implements AttributeModifierType {
    @Override
    public <T> T modify(T current, T value) {
        return value;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return WbsWandcraft.getKey("set");
    }
}
