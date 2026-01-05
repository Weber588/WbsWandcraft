package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.particles.RingParticleEffect;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.extensions.*;
import wbs.wandcraft.spell.definitions.type.SpellType;

import java.util.LinkedList;
import java.util.List;

public class FireBreathSpell extends SpellDefinition implements ContinuousCastableSpell, BurnDamageSpell, DirectionalSpell, RangedSpell, ParticleSpell {
    private static final RingParticleEffect FIRE_EFFECT = (RingParticleEffect) new RingParticleEffect()
            .setRadius(0.01)
            .setVariation(0.03)
            .setAmount(7);

    public FireBreathSpell() {
        super("fire_breath");

        addSpellType(SpellType.NETHER);

        setAttribute(COST, 100);
        setAttribute(COOLDOWN, 30 * Ticks.TICKS_PER_SECOND);

        setAttribute(FIXED_DURATION, 3 * Ticks.TICKS_PER_SECOND);
        setAttribute(MAX_DURATION, 10 * Ticks.TICKS_PER_SECOND);
        setAttribute(RANGE, 5d);
        setAttribute(COST_PER_TICK, 5);
    }

    @Override
    public String rawDescription() {
        return "Continuously breathe fire, until you stop sneaking or until the max duration is reached.";
    }

    @Override
    public void onStartCasting(CastContext context) {
        context.location().getWorld().playSound(context.player(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
    }

    @Override
    public void tick(CastContext context, int tick, int ticksLeft) {
        SpellInstance instance = context.instance();
        Player player = context.getOnlinePlayer();
        if (player == null || !player.isOnline()) {
            return;
        }

        Vector direction = getDirection(context, player, 0.2);
        double range = instance.getAttribute(RANGE);

        Location location = player.getEyeLocation();

        FIRE_EFFECT
                .setRotation(Math.random() * 360)
                .setAbout(direction)
                .setDirection(direction)
                .setSpeed(range / 4)
                .buildAndPlay(getParticle(instance), location.clone().add(direction).add(0, -0.15, 0));

        List<Entity> hitEntities = new LinkedList<>();

        double raySize = 0.3;

        RayTraceResult result;
        do {
            result = location.getWorld().rayTrace(
                    location,
                    direction,
                    range,
                    FluidCollisionMode.NEVER,
                    true,
                    raySize,
                    entity -> !entity.equals(player) && !hitEntities.contains(entity)
            );

            if (result != null) {
                Entity hitEntity = result.getHitEntity();
                if (hitEntity != null) {
                    hitEntities.add(hitEntity);
                }
            }
        } while (result != null && result.getHitEntity() != null);

        for (Entity hitEntity : hitEntities) {
            damageAndBurn(hitEntity, context);
        }
    }

    @Override
    public Particle getDefaultParticle() {
        return Particle.FLAME;
    }

    @Override
    public @NotNull String getKilledVerb() {
        return "burnt to a crisp";
    }
}
