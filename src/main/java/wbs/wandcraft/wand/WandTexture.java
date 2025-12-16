package wbs.wandcraft.wand;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.TextureProvider;
import wbs.wandcraft.WbsWandcraft;

public final class WandTexture implements Keyed, TextureProvider {
    public static final WandTexture MAGE = new WandTexture("mage");
    public static final WandTexture WIZARDRY = new WandTexture("wizardry");
    public static final WandTexture SORCERY = new WandTexture("sorcery");
    public static final WandTexture TRIDENT = new WandTexture("trident", "wizardry");
    public static final WandTexture FIRE = new WandTexture("fire").setAnimated(true);
    public static final WandTexture OVERGROWN = new WandTexture(WbsWandcraft.getKey("overgrown"), "mage", "overgrown");

    private final NamespacedKey key;
    private final String textureKey;
    private final String baseTexture;
    private boolean isAnimated;
    private boolean isBaseAnimated;

    public WandTexture(String textureKey) {
        this(textureKey, textureKey);
    }
    public WandTexture(String overlayTexture, String baseTexture) {
        this(WbsWandcraft.getKey(overlayTexture), overlayTexture, baseTexture);
    }
    public WandTexture(NamespacedKey key, String overlayTexture, String baseTexture) {
        this.key = key;
        this.textureKey = "wand_" + overlayTexture;
        this.baseTexture = "wand_" + baseTexture + "_base";
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public @NotNull String getTexture() {
        return textureKey;
    }

    public String getBaseTexture() {
        return baseTexture;
    }

    public boolean isAnimated() {
        return isAnimated;
    }

    public WandTexture setAnimated(boolean animated) {
        isAnimated = animated;
        return this;
    }

    public boolean isBaseAnimated() {
        return isBaseAnimated;
    }

    public WandTexture setBaseAnimated(boolean baseAnimated) {
        isBaseAnimated = baseAnimated;
        return this;
    }
}
