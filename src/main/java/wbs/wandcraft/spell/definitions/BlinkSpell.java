package wbs.wandcraft.spell.definitions;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.definitions.extensions.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.RangedSpell;
import wbs.wandcraft.spell.definitions.extensions.SpeedSpell;

public class BlinkSpell extends SpellDefinition implements CastableSpell, RangedSpell, SpeedSpell {
    public BlinkSpell() {
        super("blink");
        addAttribute(RANGE, 10.0);
        addAttribute(COOLDOWN, 15);
    }

    @Override
    public void cast(CastContext context) {
        Player player = context.player();
        Location loc = player.getLocation();
        World world = loc.getWorld();

        world.spawnParticle(Particle.DRAGON_BREATH, loc.add(0, 1, 0), 25, 0.15, 0.15, 0.15, 0);
        world.spawnParticle(Particle.WITCH, loc, 400, 0.6, 1, 0.6, 0);

        boolean success = WbsEntityUtil.blink(player, context.instance().getAttribute(RANGE));

        if (success) {
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
