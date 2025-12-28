package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
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
import wbs.wandcraft.spell.definitions.extensions.ContinuousCastableSpell;
import wbs.wandcraft.spell.definitions.extensions.DirectionalSpell;
import wbs.wandcraft.spell.definitions.extensions.MaterialSpell;
import wbs.wandcraft.spell.definitions.extensions.RadiusedSpell;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class ShieldSpell extends SpellDefinition implements ContinuousCastableSpell, DirectionalSpell, RadiusedSpell, MaterialSpell {
    private static final SpellAttribute<Boolean> IS_BUBBLE = new BooleanSpellAttribute("is_bubble", false)
            .setWritable(true);

    private static final NormalParticleEffect CASTING_EFFECT = (NormalParticleEffect) new NormalParticleEffect()
            .setXYZ(0.5)
            .setAmount(3);
    public static final int DURATION = 5;

    public ShieldSpell() {
        super("shield");

        setAttribute(MAX_DURATION, 30 * Ticks.TICKS_PER_SECOND);
        setAttribute(FIXED_DURATION, 5 * Ticks.TICKS_PER_SECOND);
        setAttribute(RADIUS, 4d);
        setAttribute(COST_PER_TICK, 5);
        setAttribute(IMPRECISION, 0d);
    }

    @Override
    public Component description() {
        return Component.text("Continuously breathe fire, until you stop sneaking or until the max duration is reached.");
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

        Location location = player.getEyeLocation();
        CASTING_EFFECT.play(Particle.END_ROD, location);

        List<Block> blocksInRadius = new LinkedList<>();

        Vector direction = getDirection(context, player, radius);
        Location center = player.getEyeLocation().add(direction);

        // TODO: Cache offsets
        for (double x = -radius; x <= radius; x++) {
            for (double y = -radius; y <= radius; y++) {
                for (double z = -radius; z <= radius; z++) {
                    double distance = getRadius(x, y, z);
                    if (distance < radius + (THICKNESS / 2) && distance > radius - (THICKNESS / 2)) {
                        Location blockLoc = location.clone().add(x, y, z);
                        if (!instance.getAttribute(IS_BUBBLE)) {
                            if (blockLoc.distance(center) > radius / 2) {
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
                .filter(inRadius -> inRadius.isEmpty()
                        || inRadius.isReplaceable()
                        || !MagicObjectManager.getAllActive(inRadius).isEmpty()
                )
                .forEach(inRadius -> {
                    inRadius.setType(material);

                    Collection<MagicSpawnedBlock> allActive = MagicObjectManager.getAllActive(inRadius);

                    if (allActive.isEmpty()) {
                        MagicSpawnedBlock object = new MagicSpawnedBlock(inRadius, player, context);

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
