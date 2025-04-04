package wbs.wandcraft.spell.definitions;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.definitions.extensions.*;

// TODO: Add directional spell to allow inaccuracy
public class BlinkSpell extends SpellDefinition implements CastableSpell, RangedSpell, SpeedSpell, DirectionalSpell {
    public BlinkSpell() {
        super("blink");
        addAttribute(RANGE, 10.0);
        addAttribute(COOLDOWN, 15);
        addAttribute(IMPRECISION, 5d);
    }

    @Override
    public void cast(CastContext context) {
        Player player = context.player();
        Location loc = player.getLocation();
        World world = loc.getWorld();

        world.spawnParticle(Particle.DRAGON_BREATH, loc.add(0, 1, 0), 25, 0.15, 0.15, 0.15, 0);
        world.spawnParticle(Particle.WITCH, loc, 400, 0.6, 1, 0.6, 0);

        double range = context.instance().getAttribute(RANGE);
        Vector direction = getDirection(context, range);
        Block tpLocation = WbsEntityUtil.getSafeLocation(player, player.getLocation().add(direction), range);

        if (tpLocation != null) {
            player.teleport(tpLocation.getLocation().setDirection(WbsEntityUtil.getFacingVector(player)));
            loc = player.getLocation();

            world.spawnParticle(Particle.DRAGON_BREATH, loc.add(0, 1, 0), 25, 0.15, 0.15, 0.15, 0);
            world.spawnParticle(Particle.WITCH, loc, 400, 0.6, 1, 0.6, 0);

            // TODO: Add cast sounds to SpellDefinition
            // Need to do it after teleporting or it gets cut off for the user
            // getCastSound().play(loc);
            WbsEntityUtil.push(player, context.instance().getAttribute(SPEED));
        } else {
            WbsWandcraft.getInstance().sendActionBar("Whiffed!", player);
        }
    }
}
