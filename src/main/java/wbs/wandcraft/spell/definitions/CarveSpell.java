package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEventUtils;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.LineParticleEffect;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.cost.CostUtils;
import wbs.wandcraft.objects.colliders.Collider;
import wbs.wandcraft.objects.colliders.Collision;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.ContinuousCastableSpell;
import wbs.wandcraft.spell.definitions.extensions.DamageSpell;
import wbs.wandcraft.spell.definitions.extensions.RangedSpell;
import wbs.wandcraft.spell.definitions.type.SpellType;
import wbs.wandcraft.wand.Wand;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CarveSpell extends SpellDefinition implements ContinuousCastableSpell, CastableSpell, DamageSpell, RangedSpell {
    private static final Particle.DustTransition PARTICLE_DATA = new Particle.DustTransition(
            SpellType.ARCANE.color(),
            SpellType.NETHER.color(),
            0.7f
    );
    private static final LineParticleEffect LINE_EFFECT = (LineParticleEffect) new LineParticleEffect()
            .setScaleAmount(true)
            .setAmount(4)
            .setData(PARTICLE_DATA);
    private static final NormalParticleEffect HIT_EFFECT = (NormalParticleEffect) new NormalParticleEffect()
            .setSpeed(0.03)
            .setAmount(5);
    private static final NormalParticleEffect BLOCK_BREAK_EFFECT = (NormalParticleEffect) new NormalParticleEffect()
            .setAmount(1);

    public CarveSpell() {
        super("carve");

        addSpellType(SpellType.ARCANE);
        addSpellType(SpellType.NETHER);

        setAttribute(COST, 50);
        setAttribute(COOLDOWN, 5 * Ticks.TICKS_PER_SECOND);

        setAttribute(COST_PER_TICK, 1);
        setAttribute(FIXED_DURATION, 5 * Ticks.TICKS_PER_SECOND);
        setAttribute(MAX_DURATION, 60 * Ticks.TICKS_PER_SECOND);

        setAttribute(RANGE, 8d);
        setAttribute(DAMAGE, 0.5d);
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

        handleCarveCollisions(context, beamStartLocation, facingVector, range);
    }

    private void handleCarveCollisions(CastContext context, Location beamStartLocation, Vector facingVector, double range) {
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
            carve(context, beamStartLocation, facingVector, closestCollisionDistance);
            handleCarveCollisions(context, endLocation, WbsMath.reflectVector(facingVector, closestCollision.getNormal()), range - closestCollisionDistance);
        } else {
            carve(context, beamStartLocation, facingVector, range);
        }
    }

    private void carve(CastContext context, Location beamStartLocation, Vector facingVector, double range) {
        Player player = context.player();
        World world = player.getWorld();

        Location endLocation = beamStartLocation.clone().add(facingVector);

        RayTraceResult result = world.rayTrace(
                beamStartLocation,
                facingVector,
                range,
                FluidCollisionMode.NEVER,
                true,
                0,
                entity -> !entity.equals(player) && entity instanceof Damageable
        );

        if (result != null) {
            endLocation = result.getHitPosition().toLocation(world);

            Entity hitEntity = result.getHitEntity();
            if (hitEntity != null) {
                onHitEntity(context, hitEntity);
                HIT_EFFECT.play(Particle.SMALL_FLAME, endLocation);
            }
            Block hitBlock = result.getHitBlock();
            if (hitBlock != null && hitBlock.getType().getHardness() > 0) {
                onHitBlock(context, hitBlock);
                HIT_EFFECT.play(Particle.ASH, endLocation);
                BLOCK_BREAK_EFFECT.setData(hitBlock.getBlockData());
                BLOCK_BREAK_EFFECT.play(Particle.BLOCK, endLocation);
            }
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
    }

    private static final Map<Block, BlockBreakProgress> BREAKING_PROGRESS = new HashMap<>();

    @Override
    public @NotNull String getKilledVerb() {
        return "carved into pieces";
    }

    private void onHitBlock(CastContext context, Block hitBlock) {
        Player player = context.player();
        SpellInstance instance = context.instance();

        double damage = instance.getAttribute(DAMAGE);

        BlockBreakProgress progressRecord = BREAKING_PROGRESS.get(hitBlock);
        float progress;
        if (progressRecord == null) {
            progress = 0;
        } else {
            progress = progressRecord.progress();
        }

        float hardness = hitBlock.getType().getHardness();
        progress += (float) ((float) 1 / (hardness / damage));

        if (progress >= 1) {
            PlayerInventory inventory = player.getInventory();
            ItemStack heldItem = inventory.getItemInMainHand();
            if (heldItem.isEmpty()) {
                heldItem = inventory.getItemInOffHand();
            }

            Wand wand = Wand.getIfValid(heldItem);

            if (wand != null) {
                hitBlock.breakNaturally(heldItem);
                if (hardness >= 1) {
                    CostUtils.takeCost(player, (int) hardness);
                }
            }
            BREAKING_PROGRESS.remove(hitBlock);
        } else {
            Chunk chunk = hitBlock.getChunk();
            for (Player viewer : chunk.getPlayersSeeingChunk()) {
                viewer.sendBlockDamage(hitBlock.getLocation(), progress, player);
            }

            BREAKING_PROGRESS.put(hitBlock, new BlockBreakProgress(hitBlock, Bukkit.getCurrentTick(), progress));

            int ticksBeforeDecay = 4 * Ticks.TICKS_PER_SECOND;
            WbsWandcraft.getInstance().runLater(() -> {
                BlockBreakProgress updatedProgressRecord = BREAKING_PROGRESS.get(hitBlock);
                if (updatedProgressRecord != null) {
                    if (updatedProgressRecord.lastDamagedTick > Bukkit.getCurrentTick() - ticksBeforeDecay) {
                        return;
                    }

                    float currentProgress = updatedProgressRecord.progress();
                    currentProgress -= (float) damage;
                    if (currentProgress > 0.01) {
                        BREAKING_PROGRESS.put(hitBlock, new BlockBreakProgress(hitBlock, Bukkit.getCurrentTick(), currentProgress));
                    } else {
                        currentProgress = 0f;
                        BREAKING_PROGRESS.remove(hitBlock);
                    }
                    for (Player viewer : chunk.getPlayersSeeingChunk()) {
                        viewer.sendBlockDamage(hitBlock.getLocation(), Math.clamp(currentProgress, 0, 1), player);
                    }
                }
            }, ticksBeforeDecay);
        }
    }

    private void onHitEntity(CastContext context, Entity hitEntity) {
        double damage = context.instance().getAttribute(DAMAGE);

        if (hitEntity instanceof Damageable damageable) {
            damageable.damage(
                    damage,
                    DamageSource.builder(DamageType.MAGIC)
                            .withDirectEntity(context.player())
                            .build()
            );
            damageable.setFireTicks(2 * Ticks.TICKS_PER_SECOND);
        }
    }

    @Override
    public String rawDescription() {
        return "Shoots a fine laser that can break blocks or hurt players.";
    }

    @Override
    public void registerEvents() {
        WbsEventUtils.register(WbsWandcraft.getInstance(), ChunkUnloadEvent.class, this::onChunkUnload);
    }

    private void onChunkUnload(ChunkUnloadEvent event) {
        Set<Block> blocks = BREAKING_PROGRESS.keySet();
        blocks.removeIf(block -> block.getChunk().equals(event.getChunk()));
    }

    private record BlockBreakProgress(Block block, int lastDamagedTick, float progress) {}
}
