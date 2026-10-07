package wbs.wandcraft.spell;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
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
import wbs.wandcraft.spell.attributes.AttributeHolder;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.util.MenuUtils;

import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@NullMarked
public class MagicDomain implements Keyed {
    private static Color mulColor(Color base, double factor) {
        return Color.fromRGB(
                (int) Math.clamp(base.getRed() * factor, 0, 255),
                (int) Math.clamp(base.getGreen() * factor, 0, 255),
                (int) Math.clamp(base.getBlue() * factor, 0, 255)
        );
    }

    private static TextColor color(String value) {
        return TextColor.color(Integer.valueOf(value, 16));
    }

    private static Color wandColor(String value) {
        return Color.fromRGB(Integer.valueOf(value, 16));
    }

    // TODO: Move these to a config
    public static final TextColor ARCANE_COLOUR = color("bd9a0c");
    public static final Color ARCANE_WAND_COLOR = wandColor("d7c719");

    public static final TextColor NETHER_COLOR = color("b3413f");
    public static final Color NETHER_WAND_COLOR = wandColor("95232c");

    public static final TextColor ENDER_COLOR = color("ad31c6");
    public static final Color ENDER_WAND_COLOR = wandColor("c719d7");

    public static final TextColor SCULK_COLOR = color("007494");
    public static final Color SCULK_WAND_COLOR = wandColor("5daca5");

    public static final TextColor VOID_COLOR = color("1c3f6e");
    public static final Color VOID_WAND_COLOR = wandColor("121749");

    public static final TextColor NATURE_COLOR = color("2d7922");
    public static final Color NATURE_WAND_COLOUR = wandColor("41d035");
    
    public static final MagicDomain ARCANE = new MagicDomain("arcane", ARCANE_COLOUR, ARCANE_WAND_COLOR)
            .rawDescription("""
                    The domain of order, information, and reason. Arcane magic is \
                    coordinated, efficient, and precise; the science of magic."""
            ).defaultParticle(Particle.DUST_COLOR_TRANSITION)
            .ambientParticle(Particle.ENCHANT)
            .velocityAffectedParticle(Particle.CRIT, 12.5);

    public static final MagicDomain NETHER = new MagicDomain("nether", NETHER_COLOR, NETHER_WAND_COLOR)
            .rawDescription("""
                    The domain of controlled chaos; fire and undeath. Fire can be a sign of danger, or of warmth and safety, \
                    depending on how well it's controlled."""
            ).defaultParticle(SpeedParticleEffect.class, SpeedParticleEffect::setSpeed, 0.02, Particle.SMALL_FLAME)
            .velocityAffectedParticle(Particle.FLAME)
            .ambientParticle(Particle.LAVA)
            .secondaryParticle(effect -> effect.setChance(1), Particle.FLAME);

    public static final MagicDomain ENDER = new MagicDomain("ender", ENDER_COLOR, ENDER_WAND_COLOR)
            .rawDescription("""
                    The domain of dimensionality, spacetime, and geometry. Ender magic is a corruption of natural law, \
                    treating physics and reality as obstacles to be ignored."""
            ).defaultParticle(SpeedParticleEffect.class, SpeedParticleEffect::setSpeed, 0.02, Particle.REVERSE_PORTAL)
            .secondaryParticle(SpeedParticleEffect.class, SpeedParticleEffect::setSpeed, 0.2, Particle.PORTAL)
            .ambientParticle(Particle.PORTAL)
            .velocityAffectedParticle(Particle.DRAGON_BREATH, 0.8);

    public static final MagicDomain SCULK = new MagicDomain("sculk", SCULK_COLOR, SCULK_WAND_COLOR)
            .rawDescription("""
                    The domain of corruption, contradiction, and chaos. Sculk magic makes you doubt your senses, and \
                    fight to retain control of the very magic you call forth."""
            ).defaultParticle(effect -> effect.setDynamicDataProvider(
                    (_, _) -> (float) Math.random() * Math.PI * 2),
                    Particle.SCULK_CHARGE
            )
            .ambientParticle(Particle.SCULK_SOUL)
            .velocityAffectedParticle(Particle.SCULK_CHARGE, 1.5);

    public static final MagicDomain VOID = new MagicDomain("void", VOID_COLOR, VOID_WAND_COLOR)
            .rawDescription("""
                    The domain of eternity and absence. Everything ends except for darkness, the cold, and time."""
            ).defaultParticle(Particle.SMOKE)
            .ambientParticle(Particle.SMOKE)
            .velocityAffectedParticle(Particle.SMOKE, 2);

    public static final MagicDomain NATURE = new MagicDomain("nature", NATURE_COLOR, NATURE_WAND_COLOUR)
            .rawDescription("""
                       The domain of the natural world, life, and adaptability. Natural magic respects the natural laws \
                       of reality, but uses them to its advantage."""
            ).secondaryParticle(
                    effect -> effect.setDynamicDataProvider((_, _) ->
                            mulColor(NATURE_WAND_COLOUR, new Random().nextDouble(0.8, 1.2))
                    ),
                    Particle.TINTED_LEAVES
            )
            .ambientParticle(Particle.SPORE_BLOSSOM_AIR)
            .velocityAffectedParticle(Particle.SCRAPE, 100);
    

    public static MagicDomain getOpposite(MagicDomain type) {
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
    private Particle velocityAffectedParticle = Particle.END_ROD;
    private double velocityParticleModifier = 1;
    @Nullable
    private Particle ambientParticle;

    // TODO: Clean up the particle nonsense and have a single related object that defines certain types/shapes of particle usage;
    //  For example, generic, around_player, directional_point, velocity

    protected MagicDomain(NamespacedKey key, Component displayName, TextColor textColor, Color wandColor) {
        this.key = key;
        this.displayName = displayName;
        this.textColor = textColor;
        this.wandColor = wandColor;

        WandcraftRegistries.MAGIC_DOMAINS.register(this);
    }

    MagicDomain(String nativeKey, Component displayName, TextColor textColor, Color wandColor) {
        this(WbsWandcraft.getKey(nativeKey), displayName, textColor, wandColor);
    }
    MagicDomain(String nativeKey, TextColor textColor, Color wandColor) {
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

    private MagicDomain rawDescription(String rawDescription) {
        this.rawDescription = rawDescription;
        return this;
    }

    private @Nullable String rawDescription() {
        return rawDescription;
    }

    public Component getAttributesText() {
        return getAttributesText(null);
    }
    public Component getAttributesText(@Nullable AttributeHolder holder) {
        return Component.join(JoinConfiguration.newlines(),
                MagicDomainModifiers.getMagicDomainModifiers(this).stream()
                        .filter(modifier -> holder == null || holder.hasAttribute(modifier.attribute()))
                        .map(SpellAttributeModifier::toComponent)
                        .map(c -> c.style(MenuUtils.EXTRAS_STYLE))
                        .toList()
        );
    }

    public Particle defaultParticle() {
        return defaultParticle;
    }

    public Consumer<WbsParticleEffect> defaultEffect() {
        return particleEffectModifier;
    }

    private MagicDomain defaultParticle(Particle defaultParticle) {
        return defaultParticle(_ -> {}, defaultParticle);
    }
    private MagicDomain defaultParticle(Consumer<WbsParticleEffect> effectModifier, Particle defaultParticle) {
        this.defaultParticle = defaultParticle;
        this.particleEffectModifier = effectModifier;
        return this;
    }
    private <T, R> MagicDomain defaultParticle(Class<T> clazz, BiConsumer<T, R> function, R value, Particle defaultParticle) {
        this.defaultParticle = defaultParticle;
        this.particleEffectModifier = toModifier(clazz, function, value);
        return this;
    }

    private <T, R> Consumer<WbsParticleEffect> toModifier(Class<T> clazz, BiConsumer<T, R> function, R value) {
        return effect -> {
            if (clazz.isAssignableFrom(effect.getClass())) {
                //noinspection unchecked
                function.accept((T) effect, value);
            }
        };
    }

    public @Nullable Particle secondaryParticle() {
        return secondaryParticle;
    }

    public @Nullable Consumer<WbsParticleEffect> secondaryEffect() {
        return secondaryEffectModifier;
    }

    private MagicDomain secondaryParticle(@Nullable Particle secondaryParticle) {
        return secondaryParticle(_ -> {}, secondaryParticle);
    }
    private MagicDomain secondaryParticle(Consumer<WbsParticleEffect> secondaryModifier, @Nullable Particle secondaryParticle) {
        this.secondaryParticle = secondaryParticle;
        this.secondaryEffectModifier = secondaryModifier;
        return this;
    }
    private <T, R> MagicDomain secondaryParticle(Class<T> clazz, BiConsumer<T, R> function, R value, Particle defaultParticle) {
        this.secondaryParticle = defaultParticle;
        this.secondaryEffectModifier = toModifier(clazz, function, value);
        return this;
    }

    public Particle velocityAffectedParticle() {
        return velocityAffectedParticle;
    }

    private MagicDomain velocityAffectedParticle(Particle velocityAffectedParticle) {
        this.velocityAffectedParticle = velocityAffectedParticle;
        return this;
    }

    private MagicDomain velocityAffectedParticle(Particle velocityAffectedParticle, double speedModifier) {
        this.velocityAffectedParticle = velocityAffectedParticle;
        this.velocityParticleModifier = speedModifier;
        return this;
    }

    public double velocityParticleModifier() {
        return velocityParticleModifier;
    }

    private MagicDomain velocityParticleModifier(double velocityParticleModifier) {
        this.velocityParticleModifier = velocityParticleModifier;
        return this;
    }

    private MagicDomain ambientParticle(@Nullable Particle ambientParticle) {
        this.ambientParticle = ambientParticle;
        return this;
    }

    public @Nullable Particle ambientParticle() {
        return this.ambientParticle;
    }
}
