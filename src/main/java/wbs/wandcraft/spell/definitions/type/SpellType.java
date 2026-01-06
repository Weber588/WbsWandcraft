package wbs.wandcraft.spell.definitions.type;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.string.WbsStrings;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;

@NullMarked
public class SpellType implements Keyed {
    public static final SpellType ARCANE = new SpellType("arcane", TextColor.color(0xaca55d), Color.fromRGB(0xd7c719));
    public static final SpellType NETHER = new SpellType("nether", TextColor.color(0x95232c), Color.fromRGB(0x95232c));
    public static final SpellType ENDER = new SpellType("ender", TextColor.color(0x871192), Color.fromRGB(0xc719d7));
    public static final SpellType SCULK = new SpellType("sculk", TextColor.color(0x5d8bac), Color.fromRGB(0x5daca5));
    public static final SpellType VOID = new SpellType("void", TextColor.color(0x123249), Color.fromRGB(0x121749));
    public static final SpellType NATURE = new SpellType("nature", TextColor.color(0x2c9523), Color.fromRGB(0x41d035));

    private final NamespacedKey key;
    private final Component displayName;
    private final TextColor textColor;
    private final Color wandColor;

    protected SpellType(NamespacedKey key, Component displayName, TextColor textColor, Color wandColor) {
        this.key = key;
        this.displayName = displayName;
        this.textColor = textColor;
        this.wandColor = wandColor;

        WandcraftRegistries.SPELL_TYPES.register(this);
    }

    SpellType(String nativeKey, Component displayName, TextColor textColor, Color wandColor) {
        this(WbsWandcraft.getKey(nativeKey), displayName, textColor, wandColor);
    }

    SpellType(String nativeKey, TextColor textColor, Color wandColor) {
        this(nativeKey, Component.text(WbsStrings.capitalizeAll(nativeKey.replaceAll("_", " "))), textColor, wandColor);
    }
    public Component displayName() {
        return displayName;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public Color color() {
        return Color.fromRGB(textColor.value());
    }
    public TextColor textColor() {
        return textColor;
    }
    public Color wandColor() {
        return wandColor;
    }
    public Color mulColor(double factor) {
        Color base = color();

        return Color.fromRGB(
                (int) Math.clamp(base.getRed() * factor, 0, 255),
                (int) Math.clamp(base.getGreen() * factor, 0, 255),
                (int) Math.clamp(base.getBlue() * factor, 0, 255)
        );
    }
}
