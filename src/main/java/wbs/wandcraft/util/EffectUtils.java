package wbs.wandcraft.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.Random;

public class EffectUtils {
    public static final Random RANDOM = new Random();
    private static final String CHARS_IN_ILLAGERALT = "abcdefghijklmnopqrstuvwxyz";
    private static final Key fontKey = Key.key("illageralt");

    public static @NotNull TextDisplay getGlyphDisplay(TextColor textColor, Location spawnLoc, Vector3f translation, Vector3f scale, AxisAngle4f leftRotation, AxisAngle4f rightRotation) {
        Component glyph = Component.text(CHARS_IN_ILLAGERALT.charAt(RANDOM.nextInt(CHARS_IN_ILLAGERALT.length())))
                .color(textColor)
                .font(fontKey);
        return getGlyphDisplay(glyph, spawnLoc, translation, scale, leftRotation, rightRotation);
    }
    public static @NotNull TextDisplay getGlyphDisplay(Component glyph, Location spawnLoc, Vector3f translation, Vector3f scale, AxisAngle4f leftRotation, AxisAngle4f rightRotation) {
        TextDisplay entity = spawnLoc.getWorld().createEntity(spawnLoc, TextDisplay.class);

        entity.text(glyph);
        entity.setTextOpacity((byte) 255);
        entity.setBrightness(new Display.Brightness(15, 15));
        entity.setBackgroundColor(Color.fromARGB(1, 0, 0, 0));

        entity.setTransformation(new Transformation(
                translation,
                leftRotation,
                scale,
                rightRotation
        ));
        return entity;
    }
}
