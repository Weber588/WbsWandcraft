package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.LineParticleEffect;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.pluginhooks.WbsRegionUtils;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.objects.colliders.Collider;
import wbs.wandcraft.objects.colliders.Collision;
import wbs.wandcraft.spell.definitions.extensions.BurnDamageSpell;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.ContinuousCastableSpell;
import wbs.wandcraft.spell.definitions.extensions.RangedSpell;
import wbs.wandcraft.spell.definitions.type.SpellType;

import java.util.ArrayList;
import java.util.Iterator;

public class HeatRaySpell extends SpellDefinition implements ContinuousCastableSpell, CastableSpell, BurnDamageSpell, RangedSpell {
    public static final int WATER_PER_TICK = 3;
    public static final double BEAM_RADIUS = 0.6;
    private static final Particle.DustTransition PARTICLE_DATA = new Particle.DustTransition(
            SpellType.NETHER.color(),
            SpellType.NETHER.mulColor(1.5),
            1.1f
    );
    private static final LineParticleEffect LINE_EFFECT = (LineParticleEffect) new LineParticleEffect()
            .setScaleAmount(true)
            .setRadius(BEAM_RADIUS / 2)
            .setAmount(1)
            .setData(PARTICLE_DATA);
    private static final NormalParticleEffect HIT_EFFECT = (NormalParticleEffect) new NormalParticleEffect()
            .setSpeed(0.03)
            .setAmount(2);

    public HeatRaySpell() {
        super("heat_ray");

        addSpellType(SpellType.NETHER);
        addSpellType(SpellType.NATURE);

        setAttribute(COST, 50);
        setAttribute(COOLDOWN, 5 * Ticks.TICKS_PER_SECOND);

        setAttribute(COST_PER_TICK, 1);
        setAttribute(FIXED_DURATION, 5 * Ticks.TICKS_PER_SECOND);
        setAttribute(MAX_DURATION, 60 * Ticks.TICKS_PER_SECOND);

        setAttribute(RANGE, 8d);
        setAttribute(DAMAGE, 0.5d);
        setAttribute(BURN_TIME, 2 * Ticks.TICKS_PER_SECOND);
    }

    @Override
    public void tick(CastContext context, int tick, int ticksLeft) {
        SpellInstance instance = context.instance();
        Player player = context.getOnlinePlayer();
        if (player == null) {
            return;
        }

        double range = instance.getAttribute(RANGE);
        Vector facingVector = WbsEntityUtil.getFacingVector(player, range);
        Location beamStartLocation = player.getEyeLocation();

        ArrayList<Vector> offsets = WbsMath.get3Ring(6, BEAM_RADIUS, facingVector, 0);

        for (Vector offset : offsets) {
            handleRayCollisions(context, beamStartLocation.clone().add(offset), facingVector, range, 0);
        }
    }

    private void handleRayCollisions(CastContext context, Location beamStartLocation, Vector facingVector, double range, int evaporated) {
        Location endLocation = beamStartLocation.clone().add(WbsMath.scaleVector(facingVector, range));

        Collision closestCollision = null;
        double closestCollisionDistance = Double.MAX_VALUE;
        for (Collider collider : Collider.getColliders()) {
            Collision collision = collider.getCollision(beamStartLocation, endLocation);
            if (collision != null) {
                double hitDistance = collision.getHitLocation().distance(beamStartLocation);
                if (hitDistance < closestCollisionDistance) {
                    closestCollision = collision;
                    closestCollisionDistance = hitDistance;
                }
            }
        }

        if (closestCollision != null) {
            heatRay(context, beamStartLocation, facingVector, closestCollisionDistance, evaporated == 0);
            handleRayCollisions(context, endLocation, WbsMath.reflectVector(facingVector, closestCollision.getNormal()), range - closestCollisionDistance, evaporated);
        } else {
            boolean anyEvaporated = heatRay(context, beamStartLocation, facingVector, range, evaporated == 0);
            if (anyEvaporated) {
                evaporated++;
                if (evaporated < WATER_PER_TICK) {
                    handleRayCollisions(context, beamStartLocation, facingVector, 0, evaporated);
                }
            }
        }
    }

    private boolean heatRay(CastContext context, Location beamStartLocation, Vector facingVector, double range, boolean doRayParticles) {
        Player player = context.player();
        World world = player.getWorld();

        Location endLocation = beamStartLocation.clone().add(facingVector);

        RayTraceResult result = world.rayTrace(
                beamStartLocation,
                facingVector,
                range,
                FluidCollisionMode.ALWAYS,
                true,
                0,
                entity -> !entity.equals(player) && entity instanceof Damageable
        );

        boolean evaporated = false;
        if (result != null) {
            endLocation = result.getHitPosition().toLocation(world);

            Entity hitEntity = result.getHitEntity();
            if (hitEntity != null) {
                damageAndBurn(hitEntity, context);

                HIT_EFFECT.play(Particle.SMALL_FLAME, endLocation);
            }
            Block hitBlock = result.getHitBlock();
            if (hitBlock != null) {
                if (WbsRegionUtils.canBuildAt(hitBlock.getLocation(), player)) {
                    Material material = hitBlock.getType();
                    if (material == Material.WATER) {
                        hitBlock.setType(Material.AIR);
                        HIT_EFFECT.play(Particle.CLOUD, endLocation);
                        player.getWorld().playSound(endLocation, Sound.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1, 1);
                        evaporated = true;
                    } else {
                        for (@NotNull Iterator<Recipe> it = Bukkit.recipeIterator(); it.hasNext(); ) {
                            Recipe recipe = it.next();

                            if (recipe instanceof FurnaceRecipe furnaceRecipe) {
                                Material resultMaterial = furnaceRecipe.getResult().getType();
                                if (resultMaterial.isBlock()) {
                                    if (furnaceRecipe.getInputChoice().test(ItemStack.of(material))) {
                                        hitBlock.setType(resultMaterial);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!doRayParticles) {
            return evaporated;
        }

        if (evaporated) {
            HIT_EFFECT.play(Particle.SMALL_FLAME, endLocation);
        }

        Vector offsetToWand = new Vector(-0.35, -0.55, 0.65);

        if (player.isSneaking()) {
            offsetToWand.add(new Vector(0, -0.25, -0.05));
        }
        if (context.slot() == EquipmentSlot.OFF_HAND) {
            offsetToWand.setX(offsetToWand.getX() * -1);
        }

        offsetToWand.rotateAroundY(Math.toRadians(-player.getYaw()));

        Location particleStartLocation = beamStartLocation.clone().add(offsetToWand);
        LINE_EFFECT.play(Particle.DUST_COLOR_TRANSITION, particleStartLocation, endLocation);
        return evaporated;
    }

    @Override
    public @NotNull String getKilledVerb() {
        return "burnt to a crisp";
    }

    @Override
    public String rawDescription() {
        return "Shoots a ray of heat that can evaporate water and burns entities";
    }
}
