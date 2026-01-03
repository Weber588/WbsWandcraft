package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.objects.MagicObjectManager;
import wbs.wandcraft.objects.colliders.MagicSpawnedBlock;
import wbs.wandcraft.spell.attributes.BooleanSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.extensions.*;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static wbs.wandcraft.spell.definitions.type.SpellType.ARCANE;

public class ShieldSpell extends SpellDefinition implements ContinuousCastableSpell, DirectionalSpell, RadiusedSpell, MaterialSpell, FollowableSpell {
    private static final SpellAttribute<Boolean> IS_BUBBLE = new BooleanSpellAttribute("is_bubble", false)
            .setWritable(true);

    private static final NormalParticleEffect CASTING_EFFECT = (NormalParticleEffect) new NormalParticleEffect()
            .setXYZ(0.5)
            .setChance(50)
            .setAmount(1);
    public static final int DURATION = 5;

    public ShieldSpell() {
        super("shield");

        addSpellType(ARCANE);

        setAttribute(COST, 50);
        setAttribute(COOLDOWN, 10 * Ticks.TICKS_PER_SECOND);

        setAttribute(MAX_DURATION, 30 * Ticks.TICKS_PER_SECOND);
        setAttribute(FIXED_DURATION, 5 * Ticks.TICKS_PER_SECOND);
        setAttribute(RADIUS, 4d);
        setAttribute(COST_PER_TICK, 5);
        setAttribute(IMPRECISION, 0d);
        setAttribute(FOLLOWS_PLAYER, true);

        addAttribute(IS_BUBBLE);
    }

    @Override
    public String rawDescription() {
        return "Continuously spawn an arc of blocks in the direction you're looking, blocking most magic and entities!";
    }

    @Override
    public void onStartCasting(CastContext context) {
        context.location().getWorld().playSound(context.player(), Sound.BLOCK_BEACON_AMBIENT, 1, 2);
    }

    @Override
    public void tick(CastContext context, int tick, int ticksLeft) {
        if (tick % Ticks.TICKS_PER_SECOND == 0) {
            context.location().getWorld().playSound(context.player(), Sound.BLOCK_BEACON_AMBIENT, 1, 2);
        }

        SpellInstance instance = context.instance();
        Player player = context.getOnlinePlayer();
        if (player == null || !player.isOnline()) {
            return;
        }

        double radius = instance.getAttribute(RADIUS);

        final double THICKNESS = 1;

        boolean followPlayer = context.instance().getAttribute(FOLLOWS_PLAYER);

        Location location;
        if (followPlayer) {
            location = player.getEyeLocation();
        } else {
            location = context.location();
        }

        CASTING_EFFECT.play(Particle.END_ROD, location.clone().add(0, -0.5, 0));

        List<Block> blocksInRadius = new LinkedList<>();

        Vector direction;
        if (followPlayer) {
            direction = getDirection(context, player, radius);
        }  else {
            direction = player.getLocation().subtract(context.location()).toVector();
        }
        Location center = location.clone().add(direction);

        // TODO: Cache offsets
        for (double x = -radius; x <= radius; x++) {
            for (double y = -radius; y <= radius; y++) {
                for (double z = -radius; z <= radius; z++) {
                    double distance = getRadius(x, y, z);
                    if (distance < radius + (THICKNESS / 2) && distance > radius - (THICKNESS / 2)) {
                        Location blockLoc = location.clone().add(x, y, z);
                        if (!instance.getAttribute(IS_BUBBLE)) {
                            double centerDistance = blockLoc.distance(center);
                            if (centerDistance > (radius - 1)) {
                                continue;
                            }
                        }
                        blocksInRadius.add(blockLoc.getBlock());
                    }
                }
            }
        }
        Material material = context.instance().getAttribute(MATERIAL);

        blocksInRadius.stream()
                .filter(block -> block.isEmpty()
                        || block.isReplaceable()
                        || !MagicObjectManager.getAllActive(block).isEmpty()
                )
                .forEach(block -> {
                    block.setType(material);

                    Collection<MagicSpawnedBlock> allActive = MagicObjectManager.getAllActive(block);

                    if (allActive.isEmpty()) {
                        MagicSpawnedBlock object = new MagicSpawnedBlock(block, player, context);

                        object.setRemoveBlockOnExpire(true);
                        object.setDuration(DURATION);

                        object.spawn();
                    } else {
                        allActive.forEach(obj -> obj.setDuration(obj.getAge() + DURATION));
                    }
                });
    }

    private static double getRadius(double x, double y, double z) {
        return Math.sqrt((x * x) + (y * y) + (z * z));
    }

    @Override
    public Material getDefaultMaterial() {
        return Material.PURPLE_STAINED_GLASS;
    }
}
