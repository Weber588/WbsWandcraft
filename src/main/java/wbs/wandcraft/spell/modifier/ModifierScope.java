package wbs.wandcraft.spell.modifier;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.TextureProvider;
import wbs.wandcraft.WbsWandcraft;

public enum ModifierScope implements TextureProvider {
    NEXT,
    PREVIOUS,
    LEFT,
    RIGHT,
    ABOVE,
    BELOW,
    GLOBAL,
    WAND,
    ;

    private final String texture;

    ModifierScope() {
        texture = "modifier_default";
    }
    ModifierScope(String texture) {
        this.texture = texture;
    }

    @Override
    public @NotNull String getTexture() {
        return texture;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return WbsWandcraft.getKey("scope_" + name().toLowerCase());
    }
}
