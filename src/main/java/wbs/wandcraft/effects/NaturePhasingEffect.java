package wbs.wandcraft.effects;

import io.papermc.paper.event.player.PlayerFailMoveEvent;
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.Ticks;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BoundingBox;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsEventUtils;
import wbs.utils.util.WbsLocationUtil;
import wbs.utils.util.WbsRegistryUtil;
import wbs.wandcraft.WbsWandcraft;

import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

/**
 * A better version of invisibility that hides particles and prevents mob tracking.
 */
@NullMarked
public class NaturePhasingEffect extends StatusEffect {
    // TODO: Make this configurable
    public static final int FREQUENCY = 1;

    @Override
    public Component display() {
        return Component.text("Phasing").color(NamedTextColor.YELLOW);
    }

    @Override
    public Set<PotionEffect> getPotionEffects() {
        return Set.of(
                new PotionEffect(PotionEffectType.SLOWNESS, Ticks.TICKS_PER_SECOND, 2)
        );
    }

    @Override
    public void registerEvents() {
        super.registerEvents();

        WbsEventUtils.register(WbsWandcraft.getInstance(), PlayerFailMoveEvent.class, this::onWalkThroughBlock);
        WbsEventUtils.register(WbsWandcraft.getInstance(), EntityDamageEvent.class, this::onSuffocate);
        WbsEventUtils.register(WbsWandcraft.getInstance(), EntityDamageByEntityEvent.class, this::onAttack);
    }

    private void onAttack(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        if (damager instanceof LivingEntity livingEntity) {
            if (livingEntity.getEyeLocation().getBlock().isSolid()) {
                ifPresent(livingEntity, instance -> {
                    event.setCancelled(true);
                });
            }
        }
    }

    private void onSuffocate(EntityDamageEvent event) {
        if (event.getDamageSource().getDamageType() == DamageType.IN_WALL) {
            ifPresent(event.getEntity(), instance -> {
                event.setCancelled(true);
            });
        }
    }

    @Override
    protected void onRemove(LivingEntity entity, StatusEffectInstance instance) {
        if (!(entity instanceof Player player)) {
            return;
        }

        BoundingBox phaseBox = getPhaseBox(player, 5);
        Set<Block> intersectingBlocks = WbsLocationUtil.getIntersectingBlocks(phaseBox, phaseBox.getCenter().toLocation(player.getWorld()));

        intersectingBlocks.forEach(block -> {
            player.sendBlockChange(block.getLocation(), block.getBlockData());
        });
    }

    @Override
    public boolean onTick(LivingEntity entity, StatusEffectInstance instance) {
        if (!(entity instanceof Player player)) {
            return true;
        }

        if (Bukkit.getCurrentTick() % FREQUENCY == 0) {
            int removeRadius = 1;
            BoundingBox boundingBox = getPhaseBox(player, removeRadius);
            Set<Block> intersectingBlocks = WbsLocationUtil.getIntersectingBlocks(
                    boundingBox,
                    boundingBox.getCenter().toLocation(player.getWorld())
            );

            intersectingBlocks.removeIf(Predicate.not(this::canPhase));
            intersectingBlocks.removeIf(Predicate.not(Block::isSolid));

            intersectingBlocks.forEach(block -> player.sendBlockChange(
                    block.getLocation(),
                    Material.AIR.createBlockData()
            ));

            WbsWandcraft.getInstance().runLater(() -> {
                Player updatedPlayer = Bukkit.getPlayer(player.getUniqueId());
                if (updatedPlayer != null) {
                    BoundingBox updatedBox = getPhaseBox(player, removeRadius);
                    Set<Block> newIntersectingBlocks = WbsLocationUtil.getIntersectingBlocks(updatedBox, updatedBox.getCenter().toLocation(player.getWorld()));

                    intersectingBlocks.removeAll(newIntersectingBlocks);
                    intersectingBlocks.forEach(block -> {
                        player.sendBlockChange(
                                block.getLocation(),
                                block.getBlockData()
                        );
                    });
                }
            }, FREQUENCY);
        }

        return false;
    }

    private static @NotNull BoundingBox getPhaseBox(Player player, int radius) {
        BoundingBox boundingBox = player.getBoundingBox().expand(radius, 0, radius).expandDirectional(0, 2, 0);
        if (player.isSneaking() && Bukkit.getCurrentTick() % FREQUENCY * 3 == 0) {
            boundingBox.expandDirectional(0, -0.2, 0);
        }
        return boundingBox;
    }

    private boolean canPhase(Block block) {
        if (!block.isSolid()) {
            return true;
        }

        Material material = block.getType();
        switch (material) {
            case COBBLESTONE, END_STONE, COBBLED_DEEPSLATE -> {
                return true;
            }
        }

        BlockType blockType = Objects.requireNonNull(material.asBlockType(), "Block had non-block material!");

        boolean canPhase = WbsRegistryUtil.isTagged(blockType, BlockTypeTagKeys.BASE_STONE_OVERWORLD);
        canPhase |= WbsRegistryUtil.isTagged(blockType, BlockTypeTagKeys.DIRT);
        canPhase |= blockType.key().value().toLowerCase().endsWith("_ore");

        return canPhase;
    }

    private void onWalkThroughBlock(PlayerFailMoveEvent event) {
        Player player = event.getPlayer();

        if (event.getFailReason() != PlayerFailMoveEvent.FailReason.CLIPPED_INTO_BLOCK) {
            return;
        }

        ifPresent(player, instance -> {
            BoundingBox boundingBox = player.getBoundingBox();
            Location toCenter = boundingBox.getCenter().toLocation(player.getWorld()).add(event.getTo().subtract(event.getFrom()));

            Set<Block> intersectingBlocks = WbsLocationUtil.getIntersectingBlocks(boundingBox, toCenter);

            boolean shouldAllow = intersectingBlocks.stream().allMatch(this::canPhase);
            if (shouldAllow) {
                event.setAllowed(true);
                intersectingBlocks.forEach(block -> {
                    player.getWorld().spawnParticle(Particle.BLOCK, block.getLocation().toCenterLocation(), 75, 0.4, 0.4, 0.4, block.getBlockData());
                });
            }
        });
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return WbsWandcraft.getKey("nature_phasing");
    }
}
