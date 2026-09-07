package wbs.wandcraft.resourcepack;

import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.WbsWandcraft;

@NullMarked
public interface InventoryBackgroundFontElement extends FontElement {
    char getImageChar();
    String texturePath();
    FontOffsets fontOffsets();

    @Override
    default ResourcePackObjects.FontProvider buildFontProvider() {
        FontOffsets fontOffsets = fontOffsets();

        return new ResourcePackObjects.BitmapFontProvider(
                WbsWandcraft.getInstance().namespace() + ":" + texturePath() + ".png",
                fontOffsets.ascent(),
                fontOffsets.height(),
                new String[] {String.valueOf(getImageChar())}
        );
    }
}
