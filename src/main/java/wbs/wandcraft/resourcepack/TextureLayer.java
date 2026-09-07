package wbs.wandcraft.resourcepack;

import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.WbsWandcraft;

public final class TextureLayer {
    private final String name;
    private String folder = "item";
    private String namespace = WbsWandcraft.getInstance().namespace();
    private boolean isAnimated = false;
    private @Nullable Integer defaultTint = null;

    public TextureLayer(String name) {
        this.name = name;
    }

    public String folder() {
        return folder;
    }

    public String name() {
        return name;
    }

    public boolean isAnimated() {
        return isAnimated;
    }

    public @Nullable Integer defaultTint() {
        return defaultTint;
    }

    public TextureLayer folder(String folder) {
        this.folder = folder;
        return this;
    }

    public TextureLayer isAnimated(boolean animated) {
        isAnimated = animated;
        return this;
    }

    public TextureLayer defaultTint(@Nullable Integer defaultTint) {
        this.defaultTint = defaultTint;
        return this;
    }

    public String namespace() {
        return namespace;
    }

    public TextureLayer namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }
}
