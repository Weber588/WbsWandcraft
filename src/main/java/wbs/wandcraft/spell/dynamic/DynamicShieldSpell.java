package wbs.wandcraft.spell.dynamic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.ShadowColor;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.particles.entity.TextDisplayParticleBuilder;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.MagicDomain;
import wbs.wandcraft.spell.attributes.attributable.DurationAttributable;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.effect.SpellEffectInstance;

@NullMarked
public class DynamicShieldSpell extends DynamicSpell implements CastableSpell, DurationAttributable {
    public static final Vector DEFAULT_FACING = new Vector(0, 0, 1);

    private static TextComponent getShieldText(PotionEffectType effectType) {
        return Component.empty().append(
                Component.object(ObjectContents.sprite(
                        Key.key("gui"),
                        Key.key("minecraft", "mob_effect/" + effectType.getKey().value()))
                ).shadowColor(ShadowColor.shadowColor(0, 0, 0, 0))
        );
    }

    public static final int SHIELDS_SPAWNED = 3;
    public static SpellArchetype SHIELD = new GenericSpellArchetype(
            "shield",
            Component.text("Project a ray in the direction the caster is facing, instantly affecting everything in the path."),
            DynamicShieldSpell::new
    );

    public DynamicShieldSpell(MagicDomain primary, @Nullable MagicDomain secondary) {
        super(SHIELD, primary, secondary);

        setAttribute(DURATION, 5 * Ticks.TICKS_PER_SECOND);
    }

    @Override
    public void cast(CastContext context) {
        PotionEffectType type = PotionEffectType.RESISTANCE;

        MagicDomain primaryDomain = getPrimaryDomain();
        if (primaryDomain == MagicDomain.NETHER) {
            type = PotionEffectType.FIRE_RESISTANCE;
        } else if (primaryDomain == MagicDomain.NATURE) {
            type = PotionEffectType.ABSORPTION;
        }

        TextDisplayParticleBuilder builder = (TextDisplayParticleBuilder) new TextDisplayParticleBuilder()
                .setText(getShieldText(type))
                .setAngularVelocity(new Vector(0, 0.1, 0))
                .configure(textDisplay -> {
                    textDisplay.setBillboard(Display.Billboard.FIXED);
                    textDisplay.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
                    textDisplay.setInterpolationDelay(0);
                    textDisplay.setInterpolationDuration(1);
                })
                .setMaxAge(context.instance().getAttribute(DURATION));

        Location spawnLocation = context.location().setDirection(DEFAULT_FACING)
                .add(0, -1, 0);

        Vector offset = DEFAULT_FACING.clone();

        for (int i = 0; i < SHIELDS_SPAWNED; i++) {
            spawnWithOffset(context, builder, offset, spawnLocation);
            offset.rotateAroundY(2 * Math.PI / SHIELDS_SPAWNED);
        }
    }

    private void spawnWithOffset(CastContext context, TextDisplayParticleBuilder builder, Vector offset, Location spawnLocation) {
        TextDisplay entity = builder.rotationPivot(DEFAULT_FACING)
                .editTransformation(matrix ->
                        matrix.identity()
                                .translate(DEFAULT_FACING.toVector3f())
                                .translate(TextDisplayParticleBuilder.DUMB_TEXT_DISPLAY_FIX)
                                .translate(0, -1.75f, 0)
                                .scale(6)
                )
                .playParticle(spawnLocation.clone().setDirection(offset))
                .getEntity();

        TextDisplay inner = builder.rotationPivot(DEFAULT_FACING.clone().multiply(-1))
                .editTransformation(matrix ->
                        matrix.identity()
                                .rotateY((float) Math.PI)
                                .translate(DEFAULT_FACING.toVector3f().mul(-1))
                                .translate(TextDisplayParticleBuilder.DUMB_TEXT_DISPLAY_FIX)
                                .translate(0, -1.25f, 0)
                                .scale(3)
                )
                .playParticle(spawnLocation.clone().setDirection(offset))
                .getEntity();

        inner.setTextOpacity((byte) 63);

        Player player = context.player();
        player.addPassenger(entity);
        player.addPassenger(inner);
    }

    @Override
    protected Multimap<MagicDomain, SpellEffectInstance<?>> typedEvents() {
        return HashMultimap.create();
    }
}
