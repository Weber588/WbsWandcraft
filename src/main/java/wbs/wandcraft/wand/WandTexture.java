package wbs.wandcraft.wand;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.resourcepack.DynamicItemTextureProvider;
import wbs.wandcraft.resourcepack.ResourcePackObjects;
import wbs.wandcraft.resourcepack.ResourcePackObjects.ItemModel;
import wbs.wandcraft.resourcepack.ResourcePackObjects.StaticModelReference;
import wbs.wandcraft.resourcepack.TextureLayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static wbs.wandcraft.resourcepack.ResourcePackObjects.DisplayTransform;

public final class WandTexture extends WandModelProvider implements DynamicItemTextureProvider {
    public static final WandTexture BASIC = new WandTexture("basic", '\uE777')
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
    public static final WandTexture APPRENTICE = new WandTexture("apprentice", '\uE777')
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
    public static final WandTexture MAGE = new WandTexture("mage", '\uE778');
    public static final WandTexture WIZARDRY = new WandTexture("wizardry", '\uE779')
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
    public static final WandTexture SORCERY = new WandTexture("sorcery", '\uE77A');
    public static final WandTexture TRIDENT = new WandTexture("trident", "wizardry", '\uE77B');
    public static final WandTexture FIRE = new WandTexture("fire", '\uE77C').setAnimated(true);
    public static final WandTexture WILDEN = new WandTexture("wilden", '\uE77D');
    public static final WandTexture BARBARIAN = new WandTexture("barbarian", '\uE77E');
    public static final WandTexture MIMIC = new WandTexture("mimic",'\uE77F');

    private final String textureKey;
    private final String baseTexture;

    private Map<ItemDisplayTransform, DisplayTransform> displays;
    private Map<ItemDisplayTransform, DisplayTransform> inUseDisplay;

    private boolean isAnimated;
    private boolean isBaseAnimated;

    public WandTexture(String textureKey, char backgroundChar) {
        this(textureKey, textureKey, backgroundChar);
    }
    public WandTexture(String overlayTexture, String baseTexture, char backgroundChar) {
        this(WbsWandcraft.getKey(overlayTexture), overlayTexture, baseTexture, backgroundChar);
    }
    public WandTexture(NamespacedKey key, String overlayTexture, String baseTexture, char backgroundChar) {
        super(key, backgroundChar);
        this.textureKey = "wand_" + overlayTexture;
        this.baseTexture = "wand_" + baseTexture + "_base";
    }

    @Override
    public @NotNull List<TextureLayer> getTextures() {
        return List.of(
                new TextureLayer(textureKey).isAnimated(isAnimated).defaultTint(0x008000),
                new TextureLayer(baseTexture).isAnimated(isBaseAnimated)
        );
    }

    @Override
    public Map<String, ItemModel> getModelDefinitions() {
        Map<String, ItemModel> namedModelDefinitions = new HashMap<>();

        ItemModel modelDefinition = new ItemModel(
                getModelParent(),
                getLayerResourceLocations()
        );

        if (displays != null) {
            modelDefinition.setDisplays(displays);
        }

        namedModelDefinitions.put(value(), modelDefinition);

        if (inUseDisplay != null) {
            ItemModel inUseDefinition = new ItemModel(
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
    public ResourcePackObjects.ModelReference buildBaseModel() {
        ResourcePackObjects.ModelReference defaultModel = DynamicItemTextureProvider.super.buildBaseModel();

        if (inUseDisplay != null) {
            return new ResourcePackObjects.ConditionModelReference(
                    "using_item",
                    new StaticModelReference(namespace() + ":item/" + value() + "_active", getTints()),
                    defaultModel
            );
        }

        return defaultModel;
    }
}
