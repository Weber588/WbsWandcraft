package wbs.wandcraft.spell.modifier;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.resourcepack.TextureLayer;
import wbs.wandcraft.resourcepack.TextureProvider;
import wbs.wandcraft.WbsWandcraft;

import java.util.List;

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
    public @NotNull List<TextureLayer> getTextures() {
        return List.of(
                new TextureLayer("modifier_overlay", false, 0xEC273F),
                new TextureLayer(texture)
        );
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
