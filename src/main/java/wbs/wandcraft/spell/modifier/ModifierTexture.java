package wbs.wandcraft.spell.modifier;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.TextureProvider;
import wbs.wandcraft.WbsWandcraft;

public enum ModifierTexture implements TextureProvider {
    BLANK("modifier_blank"),
    UP_ARROW("modifier_up_arrow"),
    PLUS("modifier_plus"),
    EXPLOSION,
    SKULL,
    HEART("modifier_heart"),
    X("modifier_x"),
    ;

    private final String texture;

    ModifierTexture() {
        texture = getTextureFallback();
    }
    ModifierTexture(String texture) {
        this.texture = texture;
    }

    @Override
    public @NotNull String getTexture() {
        return texture;
    }

    @NotNull
    private String getTextureFallback() {
        return "modifier_blank";
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return WbsWandcraft.getKey("modifier_" + name().toLowerCase());
    }
}
