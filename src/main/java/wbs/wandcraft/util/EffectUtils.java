package wbs.wandcraft.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import wbs.utils.util.pluginhooks.hooks.PacketEventsWrapper;
import wbs.wandcraft.context.CastContext;

import java.util.Random;
import java.util.function.Consumer;

public class EffectUtils {
    public static final Random RANDOM = new Random();
    private static final String CHARS_IN_ILLAGERALT = "abcdefghijklmnopqrstuvwxyz";
    private static final Key fontKey = Key.key("illageralt");

    public static @NotNull TextDisplay getGlyphDisplay(TextColor textColor, Location spawnLoc, Transformation transformation) {
        Component glyph = Component.text(CHARS_IN_ILLAGERALT.charAt(RANDOM.nextInt(CHARS_IN_ILLAGERALT.length())))
                .color(textColor)
                .font(fontKey);
        return getGlyphDisplay(glyph, spawnLoc, transformation);
    }
    public static @NotNull TextDisplay getGlyphDisplay(Component glyph, Location spawnLoc, Transformation transformation) {
        TextDisplay entity = spawnLoc.getWorld().createEntity(spawnLoc, TextDisplay.class);

        updateGlyphDisplay(entity, glyph, transformation);

        return entity;
    }

    public static @NotNull TextDisplay showTextDisplay(Location spawnLoc, Component glyph, Transformation transformation, Consumer<TextDisplay> preSpawn) {
        TextDisplay entity;

        PacketEventsWrapper pe = PacketEventsWrapper.get().orElse(null);
        if (pe != null) {
            entity = getGlyphDisplay(glyph, spawnLoc, transformation);
            preSpawn.accept(entity);

            pe.showFakeEntity(entity, spawnLoc.getWorld().getPlayersSeeingChunk(spawnLoc.getChunk()));
        } else {
            entity = spawnLoc.getWorld().spawn(spawnLoc, TextDisplay.class, CreatureSpawnEvent.SpawnReason.CUSTOM, display -> {
                EffectUtils.updateGlyphDisplay(display, glyph, transformation);
                preSpawn.accept(display);
            });
        }

        return entity;
    }

    public static void showFakeEntity(Player player, TextDisplay entity) {
        PacketEventsWrapper.get().ifPresent(pe -> pe.showFakeEntity(entity, player));
    }

    public static void updateGlyphDisplay(TextDisplay entity, Component glyph, Transformation transformation) {
        entity.text(glyph);
        entity.setTextOpacity((byte) 255);
        entity.setBrightness(new Display.Brightness(15, 15));
        entity.setBackgroundColor(Color.fromARGB(1, 0, 0, 0));

        entity.setTransformation(transformation);

        entity.setPersistent(false);
    }

    public static void playTeleportEffect(Location loc) {
        loc = loc.clone();
        World world = loc.getWorld();
        world.spawnParticle(Particle.DRAGON_BREATH, loc.add(0, 1, 0), 25, 0.15, 0.15, 0.15, 0, 1f);
        world.spawnParticle(Particle.WITCH, loc, 400, 0.6, 1, 0.6, 0);
    }

    public static @NonNull Vector getOffsetToWand(CastContext context) {
        return getOffsetToWand(context, context.player());
    }
    public static @NonNull Vector getOffsetToWand(CastContext context, Player player) {
        Vector offsetToWand = new Vector(-0.35, -0.55, 0.65);

        if (player.isSneaking()) {
            offsetToWand.add(new Vector(0, -0.25, -0.05));
        }
        if (context.slot() == EquipmentSlot.OFF_HAND) {
            offsetToWand.setX(offsetToWand.getX() * -1);
        }

        offsetToWand.rotateAroundY(Math.toRadians(-player.getBodyYaw()));
        return offsetToWand;
    }
}
