package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsLocationUtil;
import wbs.utils.util.WbsMath;
import wbs.utils.util.particles.LineParticleEffect;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.extensions.ContinuousCastableSpell;
import wbs.wandcraft.spell.definitions.extensions.DamageSpell;
import wbs.wandcraft.spell.definitions.extensions.DirectionalSpell;
import wbs.wandcraft.spell.definitions.extensions.RangedSpell;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ChainLightningSpell extends SpellDefinition implements ContinuousCastableSpell, DirectionalSpell, DamageSpell, RangedSpell {
    private static final LineParticleEffect EFFECT = (LineParticleEffect) new LineParticleEffect()
            .setScaleAmount(true)
            .setAmount(7);

    private static final Set<Color> COLOURS = Set.of(
            Color.fromRGB(15, 240, 240),
            Color.fromRGB(200, 160, 255)
    );

    public ChainLightningSpell() {
        super("chain_lightning");

        setAttribute(MAX_DURATION, 100);
        setAttribute(COST_PER_TICK, 3);
        setAttribute(RANGE, 5d);

        updateEffectColour();
    }

    @Override
    public Component description() {
        return Component.text("Continuously throws lightning out, sparking until it hits a ");
    }

    @Override
    public void tick(CastContext context, int tick, int ticksLeft) {
        Player player = context.getOnlinePlayer();
        if (player == null) {
            return;
        }

        double damage = context.instance().getAttribute(DAMAGE);

        if (player.isInWaterOrRainOrBubbleColumn()) {
            player.damage(damage, DamageSource.builder(DamageType.INDIRECT_MAGIC).build());
            return;
        }

        double segmentLength = 1.2;

        double unsupportedArcLength = context.instance().getAttribute(RANGE);

        LightningNode root = new LightningNode(player.getEyeLocation().add(getDirection(context, player, 0.5)));
        Location target = player.getEyeLocation().add(getDirection(context, player, unsupportedArcLength));

        LightningNode closestKnown = root;

        List<LivingEntity> ignore = new LinkedList<>();
        ignore.add(player);

        for (int i = 0; i < unsupportedArcLength * 7; i++) {
            Location random = approximateTarget(target, closestKnown.point.distance(target), unsupportedArcLength);

            closestKnown = root.getClosestNode(random);

            LightningNode next = new LightningNode(extendToward(closestKnown.point, random, segmentLength));

            Collection<LivingEntity> hits = next.point.getNearbyEntitiesByType(
                    LivingEntity.class,
                    0.1,
                    entity -> !ignore.contains(entity)
            );

            if (!hits.isEmpty()) {
                hits.forEach(entity -> {
                    DamageSource source = DamageSource.builder(DamageType.INDIRECT_MAGIC)
                            .withCausingEntity(context.player())
                            .withDamageLocation(next.point)
                            .build();

                    if (entity.isInWaterOrRainOrBubbleColumn()) {
                        entity.damage(damage * 2, source);
                    } else {
                        entity.damage(damage, source);
                    }
                    Location entityHitPoint = entity.getLocation().add(
                            Math.random() * entity.getWidth() / 2,
                            Math.random() * entity.getHeight(),
                            Math.random() * entity.getWidth() / 2
                            );

                    updateEffectColour();
                    EFFECT.play(Particle.DUST, entityHitPoint, next.point);
                });

                // ignore.addAll(hits);

                // Hit something -- extend the target from that point somewhat randomly
                target.add(getDirection(context, player, unsupportedArcLength));
            } else {
                updateEffectColour();
                EFFECT.play(Particle.DUST, closestKnown.point, next.point);
            }

            if (next.point.distance(target) < segmentLength && target.getBlock().getType() != Material.LIGHTNING_ROD) {
                return;
            }

            // Check if the branch hits any blocks and cancel
            // TODO: Make this toggleable because it could have ungodly impacts on tick speeds on bigger servers
            Vector pathToNew = next.point.toVector().subtract(closestKnown.point.toVector());
            RayTraceResult rayTraceResult = closestKnown.point.getWorld().rayTraceBlocks(closestKnown.point, pathToNew, pathToNew.length(), FluidCollisionMode.ALWAYS);
            if (rayTraceResult == null) {
                closestKnown.children.add(next);
            }
        }
    }

    private static void updateEffectColour() {
        EFFECT.setOptions(new Particle.DustOptions(WbsCollectionUtil.getRandom(COLOURS), 0.4f));
    }

    // Implementing a rapidly-exploring random tree
    private static class LightningNode {
        @NotNull
        private final Location point;
        private final List<LightningNode> children = new LinkedList<>();

        LightningNode(@NotNull Location point) {
            this.point = point;
        }

        public LightningNode getClosestNode(Location to) {
            LightningNode closest = this;
            double best = closest.point.distanceSquared(to);

            for (LightningNode child : children) {
                LightningNode compare = child.getClosestNode(to);
                double distanceSquared = compare.point.distanceSquared(to);
                if (distanceSquared <= best) {
                    closest = compare;
                    best = distanceSquared;
                }
            }

            return closest;
        }
    }

    private Location extendToward(Location start, Location target, double segmentLength) {
        Vector startToTarget = target.toVector().subtract(start.toVector());
        double distance = Math.random() * (1 + segmentLength / 2);
        return start.clone().add(WbsMath.scaleVector(startToTarget, Math.min(startToTarget.length(), distance))).add(WbsMath.randomVector(segmentLength / 3));
    }

    private Location approximateTarget(Location end, double distance, double unsupportedArcLength) {
        // Check for lightning rods nearby and force the lightning that way
        Set<Block> nearbyBlocks = WbsLocationUtil.getNearbyBlocks(end, unsupportedArcLength);
        for (Block nearbyBlock : nearbyBlocks) {
            if (nearbyBlock.getType() == Material.LIGHTNING_ROD) {
                return nearbyBlock.getLocation().add(0.5, 0.75, 0.5);
            }
        }

        return end.clone().add(WbsMath.randomVector(Math.random() * 4 / distance));
    }
}
