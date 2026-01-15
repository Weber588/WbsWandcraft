package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.DirectionalSpell;
import wbs.wandcraft.spell.definitions.extensions.RangedSpell;
import wbs.wandcraft.spell.definitions.extensions.SpeedSpell;

import static wbs.wandcraft.spell.definitions.type.SpellType.ENDER;

public class BlinkSpell extends SpellDefinition implements CastableSpell, RangedSpell, SpeedSpell, DirectionalSpell {
    public BlinkSpell() {
        super("blink");

        addSpellType(ENDER);

        setAttribute(COST, 500);
        setAttribute(COOLDOWN, 2.5 * Ticks.TICKS_PER_SECOND);

        setAttribute(RANGE, 10.0);
        setAttribute(IMPRECISION, 5d);
        setAttribute(SPEED, 1d);
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
            WbsWandcraft.getInstance().sendActionBar("No safe space found!", player);
        }
    }

    @Override
    public String rawDescription() {
        return "The caster is teleported a short distance in the direction they're facing.";
    }
}
