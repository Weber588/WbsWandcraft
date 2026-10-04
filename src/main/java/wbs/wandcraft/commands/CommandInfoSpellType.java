package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.WbsMath;
import wbs.utils.util.commands.brigadier.argument.WbsRegistrySimpleArgument;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.entity.TextDisplayParticleBuilder;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.SpellType;
import wbs.wandcraft.util.MenuUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CommandInfoSpellType extends CommandInfo<SpellType> {
    private static final WbsRegistrySimpleArgument<SpellType> SPELL_TYPE = new WbsRegistrySimpleArgument<>(
            "spell_type",
            WbsWandcraft.getInstance(),
            "spell type",
            SpellType.class,
            WandcraftRegistries.SPELL_TYPES
    ).isRequired(true);

    public static Component getSpellTypePage(SpellType type, boolean collapse) {
        Component attributeComponent = getAttributeComponent(type, collapse);

        WbsMessageBuilder builder = WbsWandcraft.getInstance().buildMessageNoPrefix(type.displayName());

        if (collapse) {
            builder.append(" ").append(attributeComponent);
        }

        builder.append(MenuUtils.LINE_BREAK)
                .append(type.description().applyFallbackStyle(MenuUtils.DESCRIPTION_STYLE));

        if (!collapse) {
            builder.append(attributeComponent);
        }
        /*
        builder = builder.onClick(ClickEvent.callback(audience -> {
            readImage(WbsStrings.capitalize(type.getKey().value()) + ".png", (Player) audience, 0.1, 1, 1);
        }, ClickCallback.Options.builder().uses(Integer.MAX_VALUE).build()));
*/
        return builder.toComponent();
    }

    public CommandInfoSpellType(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label, WandcraftRegistries.SPELL_TYPES);
        
        this.addSimpleArgument(SPELL_TYPE);
    }

    @Override
    protected Component getComponent(@UnknownNullability SpellType type, boolean collapse) {
        return getSpellTypePage(type, collapse);
    }

    private void readImage(String fileName, Player player, double scale, int granularity, double particleSize) {
        File file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            WbsWandcraft.getInstance().sendMessage("&wFile not found: " + file.getPath(), player);
            return;
        }

        BufferedImage image;
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            WbsWandcraft.getInstance().sendMessage("&wIOException occurred: \"&4" + e.getMessage() + "&w\"", player);
            e.printStackTrace();
            return;
        }

        WbsWandcraft.getInstance().sendMessage("&hFile loaded. Reading...", player);

        Map<Vector, Color> colorMap = new HashMap<>();
        int seed = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            if (y % granularity == 0) {
                seed++;
                for (int x = 0; x < image.getWidth(); x++) {
                    int rgb = image.getRGB(x, y);
                    java.awt.Color color = new java.awt.Color(rgb, true);
                    if (color.getAlpha() != 0 && (x + seed) % granularity == 0) {
                        double xScaled = WbsMath.roundTo(x * scale, 3);
                        double yScaled = WbsMath.roundTo(y * scale, 3);
                        Color bukkitColor = Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue());

                        colorMap.put(new Vector(xScaled, yScaled, 0), bukkitColor);
                    }
                }
            }
        }

        plugin.runSync(() -> {
            Location center = player.getEyeLocation().add(WbsEntityUtil.getFacingVector(player).multiply(3));
            Vector offset = new Vector(image.getWidth() * scale / 2, image.getHeight() * scale / 2, 0);

            World world = player.getWorld();

            Map<Vector, Color> rotated = new HashMap<>();

            Vector localUp = WbsEntityUtil.getLocalUp(player);
            Vector up = new Vector(0, 1, 0);

            offset = WbsMath.rotateFrom(offset, localUp, up);
            offset = WbsMath.rotateVector(offset, localUp, 0 - player.getLocation().getYaw());
            center.add(offset);

            for (Vector imagePos : colorMap.keySet()) {
                Vector rotatedPos = WbsMath.rotateFrom(imagePos, localUp, up);
                rotatedPos = WbsMath.rotateVector(rotatedPos, localUp, 0 - player.getLocation().getYaw());
                rotatedPos.multiply(-1);
                rotated.put(rotatedPos, colorMap.get(imagePos));
            }

            for (Vector pos : rotated.keySet()) {
                Location loc = center.clone().add(pos);
                loc.setDirection(loc.getDirection().multiply(-1));
                new TextDisplayParticleBuilder()
                        .setBackgroundColor(rotated.get(pos))
                        .editTransformation(t -> {
                          t.scale(new Vector3f((float) scale * 4));
                        })
                        .configure(display -> {
                            display.setBillboard(Display.Billboard.FIXED);
                        })
                        .usePackets(false)
                        .playParticle(loc, player);
            }
        });
    }

    @Override
    protected @Nullable SpellType getT(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        return SPELL_TYPE.getRequiredValue(context);
    }

    private static @NonNull Component getAttributeComponent(SpellType type, boolean collapse) {
        Component attributes = type.getAttributesText();
        TextComponent descriptionText = Component.text("Attributes: \n")
                .style(MenuUtils.EXTRAS_STYLE)
                .append(attributes);

        if (collapse) {
            return Component.text("[A]")
                    .style(MenuUtils.EXTRAS_STYLE)
                    .hoverEvent(HoverEvent.showText(descriptionText));
        } else {
            return descriptionText;
        }
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        plugin.sendMessage("Usage: &h/" + context.getInput() + " <spell>", context.getSource().getSender());
        return Command.SINGLE_SUCCESS;
    }
}
