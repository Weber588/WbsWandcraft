package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.Ticks;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.RangedSpell;
import wbs.wandcraft.spell.definitions.type.SpellType;

import java.util.Optional;

public class EmergencyTeleportSpell extends SpellDefinition implements CastableSpell, RangedSpell {

    public static final int MAX_ATTEMPTS = 10;

    public EmergencyTeleportSpell() {
        super("emergency_teleport");

        addSpellType(SpellType.ENDER);

        setAttribute(COST, 20);
        setAttribute(COOLDOWN, 15 * Ticks.TICKS_PER_SECOND);

        setAttribute(RANGE, 64d);
    }

    @Override
    public String rawDescription() {
        return "Teleports you to a random safe space, ";
    }

    @Override
    public void cast(CastContext context) {
        Player player = context.player();
        World world = player.getWorld();

        double range = context.instance().getAttribute(RANGE);

        boolean teleported = false;
        int attempts = 0;
        while (!teleported && ++attempts < MAX_ATTEMPTS) {
            double x = player.getX() + range * ((Math.random() * 2) - 1);
            double y = Math.clamp(player.getY() + range * ((Math.random() * 2) - 1), world.getMinHeight(), world.getLogicalHeight());
            double z = player.getZ() + range * ((Math.random() * 2) - 1);

            Optional<Boolean> teleportAttempt = ((CraftLivingEntity) player).getHandle().randomTeleport(x, y, z, true, PlayerTeleportEvent.TeleportCause.PLUGIN);

            teleported = teleportAttempt.orElse(false);
        }

        if (!teleported) {
            player.sendActionBar(Component.text("No safe locations!").color(NamedTextColor.RED));
        }
    }
}
