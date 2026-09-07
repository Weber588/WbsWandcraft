package wbs.wandcraft.wand.background;

import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.resourcepack.*;

import java.util.List;

@NullMarked
public class WandBackground implements Keyed, InventoryBackgroundFontElement, DynamicItemTextureProvider {
    private static final double ITEM_PIXELS_TO_INV_PIXELS = 18d / 16d;

    public static final WandBackground GENERIC_3X3 = new WandBackground(
            WbsWandcraft.getKey("generic_3x3"),
            3,
            3,
            '\uE777',
            FontOffsets.DROPPER
    );
    public static final WandBackground GENERIC_3X9 = new WandBackground(
            WbsWandcraft.getKey("generic_3x9"),
            9,
            3,
            '\uE77B',
            FontOffsets.CHEST_3x9
    );
    public static final WandBackground GENERIC_6x9 = new WandBackground(
            WbsWandcraft.getKey("generic_6x9"),
            9,
            6,
            '\uE779',
            FontOffsets.CHEST_6x9
    );
    public static final WandBackground MAGE = GENERIC_6x9; /*new WandBackground(
            WbsWandcraft.getKey("mage"),
            9,
            6,
            '\uE778',
            FontOffsets.CHEST_6x9
    );*/
    public static final WandBackground WIZARDRY = new WandBackground(
            WbsWandcraft.getKey("wizardry"),
            9,
            6,
            '\uE779',
            FontOffsets.CHEST_6x9
    );
    public static final WandBackground SORCERY = new WandBackground(
            WbsWandcraft.getKey("sorcery"),
            9,
            6,
            '\uE77A',
            FontOffsets.CHEST_6x9
    );
    public static final WandBackground WILDEN = GENERIC_6x9; /*new WandBackground(
            WbsWandcraft.getKey("wilden"),
            9,
            6,
            '\uE77B',
            FontOffsets.CHEST_6x9
    );*/
    public static final WandBackground APPRENTICE = new WandBackground(
            WbsWandcraft.getKey("apprentice"),
            9,
            3,
            '\uE77B',
            FontOffsets.CHEST_3x9
    );

    private final NamespacedKey key;
    private final int horizontalSlots;
    private final int verticalSlots;
    private final char backgroundChar;
    private final FontOffsets fontOffsets;

    public WandBackground(
            NamespacedKey key, int horizontalSlots, int verticalSlots,
            char backgroundChar,
            FontOffsets fontOffsets
    ) {
        this.key = key;
        this.horizontalSlots = horizontalSlots;
        this.verticalSlots = verticalSlots;
        this.backgroundChar = backgroundChar;
        this.fontOffsets = fontOffsets;
    }
    public Component getGUIBackground() {
        return Component.text(backgroundChar).font(WbsWandcraft.getKey("background"));
    }

    @Override
    public String texturePath() {
        return "item/" + font() + "/" + getKey().value();
    }

    @Override
    public char getImageChar() {
        return backgroundChar;
    }

    @Override
    public FontOffsets fontOffsets() {
        return fontOffsets;
    }

    @Override
    public String font() {
        return "background";
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public List<TextureLayer> getTextures() {
        return List.of(
                new TextureLayer(getKey().value()).folder("item/background")
        );
    }

    @Override
    public ResourcePackObjects.ModelReference buildBaseModel() {
        // Move the texture down v-1 slots, so the top of the texture is in the items actual slot, not above
        double yOffset = -(verticalSlots - 1) * ITEM_PIXELS_TO_INV_PIXELS;

        // Since the item renders 1 pixel to the top right from the start of the inventory, offset it back 1 pixel
        double xOffset = -1d/16d;
        yOffset -= 1d/16d;

        return DynamicItemTextureProvider.super.buildBaseModel().transformation(
                new ResourcePackObjects.Transformation()
                        .scale(
                                horizontalSlots * ITEM_PIXELS_TO_INV_PIXELS,
                                verticalSlots * ITEM_PIXELS_TO_INV_PIXELS,
                                1
                        )
                        .translation(
                                xOffset,
                                yOffset,
                                0
                        )
        );
    }

    @Override
    public @Nullable String subfolder() {
        return "background";
    }
}
