package wbs.wandcraft.spell.dynamic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.Ticks;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.MagicDomain;
import wbs.wandcraft.spell.attributes.attributable.DirectionAttributable;
import wbs.wandcraft.spell.attributes.attributable.DurationAttributable;
import wbs.wandcraft.spell.attributes.attributable.RangeAttributable;
import wbs.wandcraft.spell.attributes.attributable.SpeedAttributable;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.effect.SpellEffectInstance;

import java.util.Objects;

@NullMarked
public class DynamicSpellDash extends DynamicSpell implements CastableSpell, RangeAttributable, SpeedAttributable, DurationAttributable, DirectionAttributable {
    public static SpellArchetype DASH = new GenericSpellArchetype(
            "dash",
            Component.text("Teleport a short distance in the direction the caster is facing."),
            DynamicSpellDash::new
    );
    public DynamicSpellDash(MagicDomain primary, @Nullable MagicDomain secondary) {
        super(DASH, primary, secondary);

        setAttribute(COST, 50);
        setAttribute(COOLDOWN, Ticks.TICKS_PER_SECOND);

        setAttribute(IMPRECISION, 5d);
        setAttribute(SPEED, 3d);
        setAttribute(DURATION, Ticks.TICKS_PER_SECOND * 0.25);
    }

    @Override
    public void cast(CastContext context) {
        Player player = context.player();

        WbsParticleEffect effect = new NormalParticleEffect().setSpeed(0.5).setAmount(160);
        WbsParticleGroup startingParticle = getParticleGroup(effect, effect, true);

        startingParticle.play(player.getLocation());

        // player.getWorld().spawnParticle(Particle.INSTANT_EFFECT, player.getLocation(), 160, 0, 0, 0, 0.5, new Particle.Spell(Color.WHITE, 1.5f));
        double speed = context.instance().getAttribute(SPEED);
        Vector direction = getClampedDirection(context, 0, speed);

        player.setVelocity(direction.setY(player.getVelocity().getY()));

        player.setFallDistance(0);
        WbsParticleGroup persistentParticle = getParticleGroup(new NormalParticleEffect()
                .setXYZ(0.4)
                .setY(1)
                .setAmount(10)
        );

        AttributeModifier dragDecrease = applyModifier(Attribute.AIR_DRAG_MODIFIER, player, "reduce_air_drag", -5);
        AttributeModifier frictionDecrease = applyModifier(Attribute.FRICTION_MODIFIER, player, "reduce_friction", -5);

        WbsWandcraft.getInstance().runLater(() -> {
            clearAttribute(player, Attribute.AIR_DRAG_MODIFIER, dragDecrease);
            clearAttribute(player, Attribute.FRICTION_MODIFIER, frictionDecrease);

            AttributeModifier dragIncrease = applyModifier(Attribute.AIR_DRAG_MODIFIER, player, "increase_drag", 5);
            WbsWandcraft.getInstance().runLater(() -> {
                clearAttribute(player, Attribute.AIR_DRAG_MODIFIER, dragIncrease);
                Player updatedPlayer = player.getPlayer();
                if (updatedPlayer != null) {
                    Vector velocity = updatedPlayer.getVelocity();
                    if (velocity.clone().setY(0).lengthSquared() > speed / 4) {
                        updatedPlayer.setVelocity(new Vector(0, velocity.getY(), 0));
                    }
                }
            }, 5);
        }, context.instance().getAttribute(DURATION));
    }

    private static void clearAttribute(Player player, Attribute attribute, AttributeModifier modifier) {
        player = player.getPlayer();
        if (player != null) {
            clearAttribute(player, attribute, modifier.key());
        }
    }
    private static void clearAttribute(Player player, Attribute attribute, Key key) {
        player = player.getPlayer();
        if (player != null) {
            clearAttribute(player.getAttribute(attribute), key);
        }
    }
    private static void clearAttribute(@Nullable AttributeInstance instance, Key key) {
        if (instance != null) {
            instance.removeModifier(key);
        }
    }

    private static AttributeModifier applyModifier(Attribute attribute, Player player, String key, int amount) {
        AttributeInstance instance = Objects.requireNonNull(player.getAttribute(attribute));
        AttributeModifier modifier = new AttributeModifier(WbsWandcraft.getKey(key), amount, AttributeModifier.Operation.ADD_NUMBER);
        clearAttribute(instance, modifier.getKey());
        instance.addTransientModifier(modifier);
        return modifier;
    }

    @Override
    protected Multimap<MagicDomain, SpellEffectInstance<?>> typedEvents() {
        return HashMultimap.create();
    }

    @Override
    protected String rawDescription() {
        return "The caster is thrown in the direction they're facing";
    }
}
