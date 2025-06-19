package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import wbs.utils.util.particles.RingParticleEffect;
import wbs.wandcraft.spell.definitions.extensions.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class FireBreathSpell extends SpellDefinition implements ContinuousCastableSpell, DamageSpell, DirectionalSpell, RangedSpell, BurnTimeSpell, ParticleSpell {
    private static final RingParticleEffect FIRE_EFFECT = (RingParticleEffect) new RingParticleEffect()
            .setRadius(0.2)
            .setVariation(0.03)
            .setAmount(3);

    public FireBreathSpell() {
        super("fire_breath");

        setAttribute(DURATION, 100);
        setAttribute(RANGE, 5d);
    }

    @Override
    public Component description() {
        return Component.text("Continuously breathe fire, until you stop sneaking or until the max duration is reached.");
    }

    @Override
    public void onStartCasting(CastContext context) {
        context.location().getWorld().playSound(context.player(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
    }

    @Override
    public void tick(CastContext context, int tick, int ticksLeft) {
        SpellInstance instance = context.instance();
        Player player = Objects.requireNonNull(Bukkit.getPlayer(context.player().getUniqueId()));

        Vector direction = getDirection(context, player, 0.2);
        double range = instance.getAttribute(RANGE);

        Location location = player.getEyeLocation();

        FIRE_EFFECT
                .setRotation(Math.random() * 360)
                .setAbout(direction)
                .setDirection(direction)
                .setSpeed(range / 8)
                .buildAndPlay(instance.getAttribute(PARTICLE, getDefaultParticle()), location.clone().add(direction));

        List<Entity> hitEntities = new LinkedList<>();

        RayTraceResult result;
        do {
            result = location.getWorld().rayTrace(
                    location,
                    direction,
                    range,
                    FluidCollisionMode.NEVER,
                    true,
                    0,
                    entity -> !entity.equals(player) && !hitEntities.contains(entity)
            );

            if (result != null) {
                Entity hitEntity = result.getHitEntity();
                if (hitEntity != null) {
                    hitEntities.add(hitEntity);
                }
            }
        } while (result != null && result.getHitEntity() != null);

        double damage = instance.getAttribute(DAMAGE);
        int burnTime = instance.getAttribute(BURN_TIME);

        for (Entity hitEntity : hitEntities) {
            hitEntity.setFireTicks(burnTime);
            if (hitEntity instanceof Damageable damageable) {
                damageable.damage(damage, player);
            }
        }
    }

    @Override
    public void onStopCasting(CastContext context) {

    }

    @Override
    public Particle getDefaultParticle() {
        return Particle.FLAME;
    }
}
