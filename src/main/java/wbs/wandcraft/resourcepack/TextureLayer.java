package wbs.wandcraft.resourcepack;

import org.jetbrains.annotations.Nullable;

public record TextureLayer(String name, boolean isAnimated, @Nullable Integer defaultTint) {
    public TextureLayer(String name, boolean isAnimated) {
        this(name, isAnimated, null);
    }
    public TextureLayer(String name) {
        this(name, false);
    }
}
