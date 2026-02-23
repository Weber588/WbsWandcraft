package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.WbsLocationUtil;
import wbs.utils.util.entities.selector.EntitySelector;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.SphereParticleEffect;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.objects.MagicObjectManager;
import wbs.wandcraft.objects.generics.DynamicMagicObject;
import wbs.wandcraft.objects.generics.DynamicProjectileObject;
import wbs.wandcraft.objects.generics.MagicObject;
import wbs.wandcraft.spell.definitions.extensions.CustomProjectileSpell;
import wbs.wandcraft.spell.definitions.extensions.DamageSpell;
import wbs.wandcraft.spell.definitions.extensions.ForceSpell;
import wbs.wandcraft.spell.definitions.extensions.RadiusedSpell;
import wbs.wandcraft.spell.definitions.type.SpellType;
import wbs.wandcraft.spell.event.SpellTriggeredEvents;

import java.util.List;
import java.util.Set;

public class BlackHoleSpell extends SpellDefinition implements CustomProjectileSpell, RadiusedSpell, DamageSpell, ForceSpell {

    public static final float MAX_HARDNESS = Material.ANCIENT_DEBRIS.getHardness() - 0.1f;

    public BlackHoleSpell() {
        super("black_hole");

        addSpellType(SpellType.VOID);

        setAttribute(COST, 1000);
        setAttribute(COOLDOWN, 30 * Ticks.TICKS_PER_SECOND);

        setAttribute(RADIUS, 4d);
        setAttribute(FORCE, 0.8);
        setAttribute(GRAVITY, 0d);
        setAttribute(SPEED, 0.1d);
        setAttribute(IMPRECISION, 2d);
    }

    @Override
    public String rawDescription() {
        return "Fires a slow moving black hole that eats blocks and entities in its path.";
    }

    @Override
    public void configure(DynamicProjectileObject projectile, CastContext context) {
        SpellTriggeredEvents.ON_HIT_TRIGGER.registerAnonymous(context.instance(), result -> {
            Entity hitEntity = result.getHitEntity();

            if (hitEntity instanceof Damageable damageable) {
                damage(context, damageable);
            }
        });
    }

    @Override
    public @NotNull DynamicProjectileObject buildProjectile(CastContext context, Player player) {
        return new BlackHoleObject(player.getEyeLocation(), context);
    }

    @Override
    public Particle getDefaultParticle() {
        return Particle.SQUID_INK;
    }

    @Override
    public boolean expireOnHitEntity() {
        return false;
    }

    public static class BlackHoleObject extends DynamicProjectileObject {
        private final SphereParticleEffect sphereEffect = new SphereParticleEffect();
        private final NormalParticleEffect effect = new NormalParticleEffect();

        private EntitySelector<Entity, ?> selector;

        private boolean targetEntities = true;
        private boolean targetProjectiles = true;

        public BlackHoleObject(Location location, CastContext context) {
            super(location, context.player(), context);

            sphereEffect.setRelative(true);
            sphereEffect.setAmount(70);

            effect.setAmount(1);
            effect.setXYZ(0);
        }

        @Override
        protected void onSpawn() {
            sphereEffect.setSpeed(context.instance().getAttribute(FORCE));
            selector = new RadiusSelector<>(Entity.class)
                    .setRange(getEntityRadius())
                    .setPredicate(entity -> (!(entity instanceof Player player) || (player.getGameMode() != GameMode.CREATIVE && player.getGameMode() != GameMode.SPECTATOR)))
                    .exclude(caster);
        }

        private double getEntityRadius() {
            return context.instance().getAttribute(RADIUS) * 3;
        }

        private Double getBlockRadius() {
            return context.instance().getAttribute(RADIUS);
        }

        // Minecraft calculates forces on living entities stronger than on
        // other things, including DynamicMagicObjects & arrows/projectiles.
        // Therefore, the force needs to be multiplied to make it consistent.
        private static final double PROJECTILE_MULTIPLIER = 15;

        @Override
        protected boolean onStep(int step, int stepsThisTick) {
            boolean cancel = super.onStep(step, stepsThisTick);
            if (cancel) {
                return true;
            }

            if (step != 0) {
                return false;
            }

            if (getAge() % 3 == 0) {
                effect.play(Particle.SQUID_INK, getLocation());
            }

            if (getAge() > 2 * Ticks.TICKS_PER_SECOND) {
                selector.unexclude(caster);
            }

            sphereEffect.setRotation(getAge() * 2);
            // Where it will be soon, adjusted for height
            Location spawnLocation = getLocation().add(getVelocity().multiply(Ticks.TICKS_PER_SECOND * 1.5)).add(0, -0.25, 0);
            sphereEffect.buildAndPlay(Particle.PORTAL, spawnLocation);

            double entityRadius = getEntityRadius();
            double blockRadius = getBlockRadius();

            if (targetProjectiles) {
                List<MagicObject> objects = MagicObjectManager.getNearbyActive(getLocation(), entityRadius);

                for (MagicObject obj : objects) {
                    if (obj instanceof BlackHoleObject) {
                        continue;
                    }

                    if (obj instanceof DynamicMagicObject proj) {
                        proj.applyForce(getPullForce(proj.getLocation()).multiply(PROJECTILE_MULTIPLIER));
                        //    proj.setVelocity(applyGravity(proj.getVelocity(), proj.getLocation(), null));
                    }
                }
            }

            if (targetEntities) {
                @NotNull List<? extends Entity> nearbyEntities = selector.select(getLocation());

                for (Entity entity : nearbyEntities) {
                    if (entity instanceof Projectile proj) {
                        if (targetProjectiles) {
                            Vector velocity = proj.getVelocity();

                            proj.setVelocity(applyGravity(velocity, proj.getLocation(), null).multiply(PROJECTILE_MULTIPLIER));
                        }
                    } else {
                        Vector velocity = entity.getVelocity();

                        entity.setVelocity(applyGravity(velocity, entity.getLocation(), entity));
                        // TODO: Check if it's a player, and if so, use explosion packets to make it feel smoother
                    }
                }
            }

            Set<Block> intersectingBlocks = WbsLocationUtil.getNearbyBlocksSphere(getLocation(), blockRadius);

            intersectingBlocks.stream()
                    .filter(block -> block.getType().getHardness() >= 0 || block.isLiquid())
                    .filter(block -> !block.getType().isAir())
                    .filter(block -> {
                        if (block.isLiquid()) {

                        }
                        float hardness = block.getType().getHardness();

                        if (hardness > MAX_HARDNESS || hardness < 0) {
                            return false;
                        }

                        double distanceSquared = block.getLocation().toCenterLocation().distanceSquared(location);
                        double scaledHardness = hardness * distanceSquared * distanceSquared;

                        return scaledHardness <= MAX_HARDNESS;
                    })
                    .forEach(Block::breakNaturally);

            return false;
        }

        private Vector getPullForce(Location other) {
            Vector pullForce = getLocation().subtract(other).toVector();
            double distSquared = pullForce.lengthSquared();
            pullForce.normalize().multiply(context.instance().getAttribute(FORCE) / distSquared);

            return pullForce;
        }

        private Vector applyGravity(Vector current, Location location, @Nullable Entity entity) {
            Vector pullForce = getPullForce(location);
            Vector newVelocity = current.clone().add(pullForce);

            double currentLength = current.length();
            double force = context.instance().getAttribute(FORCE);
            if (currentLength > force || newVelocity.length() > force) { // Don't speed up after reaching speed else
                newVelocity.normalize().multiply(currentLength);
                if (entity != null && entity.getLocation().distanceSquared(getLocation()) <= 1) {
                    entity.setFallDistance(0);
                }
            }

            return newVelocity;
        }
    }
}
