package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.DirectionalSpell;
import wbs.wandcraft.spell.definitions.extensions.SpeedSpell;

import static wbs.wandcraft.spell.definitions.type.SpellType.ENDER;
import static wbs.wandcraft.spell.definitions.type.SpellType.VOID;

public class LeapSpell extends SpellDefinition implements CastableSpell, DirectionalSpell, SpeedSpell {
    public LeapSpell() {
        super("leap");

        addSpellType(VOID);
        addSpellType(ENDER);

        setAttribute(COST, 50);
        setAttribute(COOLDOWN, Ticks.TICKS_PER_SECOND);

        setAttribute(IMPRECISION, 5d);
        setAttribute(SPEED, 1.5d);
    }

    @Override
    public void cast(CastContext context) {
        Player player = context.player();
        player.getWorld().spawnParticle(Particle.INSTANT_EFFECT, player.getLocation(), 160, 0, 0, 0, 0.5);
        player.setVelocity(getDirection(context, context.instance().getAttribute(SPEED)).add(player.getVelocity().multiply(0.5)));

        player.setFallDistance(0);

        //noinspection deprecation
        if (player.isOnGround()) {
            new BukkitRunnable() {
                int escape = 0;
                @Override
                public void run() {
                    player.getWorld().spawnParticle(Particle.INSTANT_EFFECT, player.getLocation().add(0, 1, 0), 10, 0.4, 1, 0.4, 0);

                    escape++;

                    //noinspection deprecation
                    if (escape > 1000 || !player.isOnline() || player.isFlying() || (player.isOnGround() && escape >= 5)) {
                        cancel();
                    }
                }
            }.runTaskTimer(WbsWandcraft.getInstance(), 2L, 2L);
        }
    }

    @Override
    public String rawDescription() {
        return "The caster is thrown in the direction they're facing, and takes no fall damage.";
    }
}
