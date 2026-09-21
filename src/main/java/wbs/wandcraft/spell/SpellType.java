package wbs.wandcraft.spell;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.particles.SpeedParticleEffect;
import wbs.utils.util.particles.WbsParticleEffect;
import wbs.utils.util.string.WbsStrings;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;

import java.util.Random;
import java.util.function.Consumer;

@NullMarked
public class SpellType implements Keyed {
    private static Color mulColor(Color base, double factor) {
        return Color.fromRGB(
                (int) Math.clamp(base.getRed() * factor, 0, 255),
                (int) Math.clamp(base.getGreen() * factor, 0, 255),
                (int) Math.clamp(base.getBlue() * factor, 0, 255)
        );
    }

    public static final SpellType ARCANE = new SpellType("arcane", TextColor.color(0xd3b400), Color.fromRGB(0xd7c719))
            .rawDescription("""
                    The domain of order, information, and reason. Arcane magic is \
                    coordinated, efficient, and precise; the science of magic."""
            ).defaultParticle(Particle.DUST_COLOR_TRANSITION);
    public static final SpellType NETHER = new SpellType("nether", TextColor.color(0xa6001b), Color.fromRGB(0x95232c))
            .rawDescription("""
                    The domain of controlled chaos; fire and undeath. Fire can be a sign of danger, or of warmth and safety, \
                    depending on how well it's controlled."""
            ).defaultParticle(effect -> {
                if (effect instanceof SpeedParticleEffect sEffect) {
                    sEffect.setSpeed(0.02);
                }
            }, Particle.SMALL_FLAME)
            .secondaryParticle(effect -> effect.setChance(1), Particle.FLAME);
    public static final SpellType ENDER = new SpellType("ender", TextColor.color(0x8e009c), Color.fromRGB(0xc719d7))
            .rawDescription("""
                    The domain of dimensionality, spacetime, and geometry. Ender magic is a corruption of natural law, \
                    treating physics and reality as obstacles to be ignored."""
            ).defaultParticle(effect -> {
                if (effect instanceof SpeedParticleEffect sEffect) {
                    sEffect.setSpeed(0.02);
                }
            }, Particle.REVERSE_PORTAL)
            .secondaryParticle(effect -> {
                effect.setChance(1);
                if (effect instanceof SpeedParticleEffect sEffect) {
                    sEffect.setSpeed(0.2);
                }
            }, Particle.PORTAL);
    public static final SpellType SCULK = new SpellType("sculk", TextColor.color(0x80c4e3), Color.fromRGB(0x5daca5))
            .rawDescription("""
                    The domain of corruption, contradiction, and chaos. Sculk magic makes you doubt your senses, and \
                    fight to retain control of the very magic you call forth."""
            ).defaultParticle(effect -> effect.setDynamicDataProvider(
                    (_, _) -> (float) Math.random() * Math.PI * 2),
                    Particle.SCULK_CHARGE
            );
    public static final SpellType VOID = new SpellType("void", TextColor.color(0x00325d), Color.fromRGB(0x121749))
            .rawDescription("""
                    The domain of eternity and absence. Everything ends except for darkness, the cold, and time."""
            ).defaultParticle(Particle.LARGE_SMOKE);

    public static final SpellType NATURE;

    static {
        Color natureColour = Color.fromRGB(0x41d035);
        NATURE = new SpellType("nature", TextColor.color(0x009a00), natureColour)
                .rawDescription("""
                        The domain of the natural world, life, and adaptability. Natural magic respects the natural laws \
                        of reality, but uses them to its advantage."""
                ).secondaryParticle(
                        effect -> effect.setDynamicDataProvider((_, _) ->
                                mulColor(natureColour, new Random().nextDouble(0.9, 1.1))
                        ),
                        Particle.TINTED_LEAVES
                );
    }

    public static SpellType getOpposite(SpellType type) {
        if (type == ARCANE) {
            return SCULK;
        } else if (type == NETHER) {
            return VOID;
        } else if (type == ENDER) {
            return NATURE;
        } else if (type == SCULK) {
            return ARCANE;
        } else if (type == VOID) {
            return NETHER;
        } else if (type == NATURE) {
            return ENDER;
        }
        throw new IllegalArgumentException("Not a registered spell type.");
    }
    private final NamespacedKey key;
    private final Component displayName;
    private final TextColor textColor;

    private final Color wandColor;
    // TODO: Make all below fields configurable
    @Nullable
    private String rawDescription;
    private Particle defaultParticle = Particle.INSTANT_EFFECT;
    private Consumer<WbsParticleEffect> particleEffectModifier = _ -> {};
    @Nullable
    private Particle secondaryParticle = null;
    @Nullable
    private Consumer<WbsParticleEffect> secondaryEffectModifier = null;

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
        this(nativeKey, Component.text(WbsStrings.capitalizeAll(nativeKey.replace("_", " "))), textColor, wandColor);
    }

    public Component displayName() {
        return displayName
                .color(textColor());
    }

    @Override
    public NamespacedKey getKey() {
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

        return mulColor(base, factor);
    }

    public Component description() {
        return rawDescription == null ? displayName : MiniMessage.miniMessage().deserialize(rawDescription);
    }

    SpellType rawDescription(String rawDescription) {
        this.rawDescription = rawDescription;
        return this;
    }

    private @Nullable String rawDescription() {
        return rawDescription;
    }

    public Component getAttributesText() {
        return Component.join(JoinConfiguration.newlines(),
                SpellTypeModifiers.getSpellTypeModifiers(this).stream()
                        .map(SpellAttributeModifier::toComponent)
                        .map(c -> c.color(NamedTextColor.GOLD))
                        .toList()
        );
    }

    public Particle defaultParticle() {
        return defaultParticle;
    }

    public Consumer<WbsParticleEffect> defaultEffect() {
        return particleEffectModifier;
    }

    public SpellType defaultParticle(Particle defaultParticle) {
        return defaultParticle(_ -> {}, defaultParticle);
    }
    public SpellType defaultParticle(Consumer<WbsParticleEffect> effectModifier, Particle defaultParticle) {
        this.defaultParticle = defaultParticle;
        this.particleEffectModifier = effectModifier;
        return this;
    }

    public @Nullable Particle secondaryParticle() {
        return secondaryParticle;
    }

    public @Nullable Consumer<WbsParticleEffect> secondaryEffect() {
        return secondaryEffectModifier;
    }

    public SpellType secondaryParticle(@Nullable Particle secondaryParticle) {
        return secondaryParticle(_ -> {}, secondaryParticle);
    }
    public SpellType secondaryParticle(Consumer<WbsParticleEffect> secondaryModifier, @Nullable Particle secondaryParticle) {
        this.secondaryParticle = secondaryParticle;
        this.secondaryEffectModifier = secondaryModifier;
        return this;
    }
}
