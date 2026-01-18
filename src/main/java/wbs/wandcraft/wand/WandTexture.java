package wbs.wandcraft.wand;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.resourcepack.ResourcePackObjects;
import wbs.wandcraft.resourcepack.ResourcePackObjects.ItemModelDefinition;
import wbs.wandcraft.resourcepack.ResourcePackObjects.StaticModel;
import wbs.wandcraft.resourcepack.TextureLayer;
import wbs.wandcraft.resourcepack.FlatItemProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static wbs.wandcraft.resourcepack.ResourcePackObjects.DisplayTransform;

public final class WandTexture implements Keyed, FlatItemProvider {
    public static final WandTexture BASIC = new WandTexture("basic")
            .addInUseDisplay(ItemDisplayTransform.FIRSTPERSON_LEFTHAND, new DisplayTransform()
                    .scale(0.68, 0.68, 0.68)
                    .translation(0.5, 3.2, 1.13)
                    .rotation(0, 80, 30)
            )
            .addInUseDisplay(ItemDisplayTransform.FIRSTPERSON_RIGHTHAND, new DisplayTransform()
                    .scale(0.68, 0.68, 0.68)
                    .translation(0.5, 3.2, 1.13)
                    .rotation(0, -80, -30)
            );
    public static final WandTexture MAGE = new WandTexture("mage");
    public static final WandTexture WIZARDRY = new WandTexture("wizardry")
            .addDisplay(ItemDisplayTransform.THIRDPERSON_LEFTHAND, new DisplayTransform()
                    .scale(1, 1, 1)
                    .translation(0, 3.0, 0.5)
                    .rotation(0, 90, -55)
            )
            .addDisplay(ItemDisplayTransform.THIRDPERSON_RIGHTHAND, new DisplayTransform()
                    .scale(1, 1, 1)
                    .translation(0, 3.0, 0.5)
                    .rotation(0, -90, 55)
            );
    public static final WandTexture SORCERY = new WandTexture("sorcery");
    public static final WandTexture TRIDENT = new WandTexture("trident", "wizardry");
    public static final WandTexture FIRE = new WandTexture("fire").setAnimated(true);
    public static final WandTexture OVERGROWN = new WandTexture(WbsWandcraft.getKey("overgrown"), "mage", "overgrown");

    private final NamespacedKey key;
    private final String textureKey;
    private final String baseTexture;

    private Map<ItemDisplayTransform, DisplayTransform> displays;
    private Map<ItemDisplayTransform, DisplayTransform> inUseDisplay;

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
    public @NotNull List<TextureLayer> getTextures() {
        return List.of(
                new TextureLayer(textureKey, isAnimated, 0x008000),
                new TextureLayer(baseTexture, isBaseAnimated)
        );
    }

    @Override
    public Map<String, ItemModelDefinition> getModelDefinitions() {
        Map<String, ItemModelDefinition> namedModelDefinitions = new HashMap<>();

        ItemModelDefinition modelDefinition = new ItemModelDefinition(
                getModelParent(),
                getLayerResourceLocations()
        );

        if (displays != null) {
            modelDefinition.setDisplays(displays);
        }

        namedModelDefinitions.put(value(), modelDefinition);

        if (inUseDisplay != null) {
            ItemModelDefinition inUseDefinition = new ItemModelDefinition(
                    getModelParent(),
                    getLayerResourceLocations()
            );

            inUseDefinition.setDisplays(inUseDisplay);
            namedModelDefinitions.put(value() + "_active", inUseDefinition);
        }


        return namedModelDefinitions;
    }

    @Override
    public @NotNull String getModelParent() {
        return "minecraft:item/handheld";
    }

    public WandTexture setAnimated(boolean animated) {
        isAnimated = animated;
        return this;
    }

    public WandTexture addDisplay(ItemDisplayTransform display, DisplayTransform transform) {
        if (this.displays == null) {
            displays = new HashMap<>();
        }
        this.displays.put(display, transform);
        return this;
    }

    public WandTexture addInUseDisplay(ItemDisplayTransform display, DisplayTransform transform) {
        if (this.inUseDisplay == null) {
            inUseDisplay = new HashMap<>();
        }
        this.inUseDisplay.put(display, transform);
        return this;
    }

    @Override
    public ResourcePackObjects.Model buildBaseModel() {
        ResourcePackObjects.Model defaultModel = FlatItemProvider.super.buildBaseModel();

        if (inUseDisplay != null) {
            return new ResourcePackObjects.ConditionModel(
                    "using_item",
                    new StaticModel(namespace() + ":item/" + value() + "_active", getTints()),
                    defaultModel
            );
        }

        return defaultModel;
    }
}
