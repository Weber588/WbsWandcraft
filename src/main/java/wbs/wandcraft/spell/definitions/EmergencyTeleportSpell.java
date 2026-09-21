package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.Ticks;
import org.bukkit.World;
import org.bukkit.entity.Player;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.SpellType;
import wbs.wandcraft.spell.attributes.attributable.RangeAttributable;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.util.EntityUtil;

public class EmergencyTeleportSpell extends SpellDefinition implements CastableSpell, RangeAttributable {

    public EmergencyTeleportSpell() {
        super("emergency_teleport");

        addSpellType(SpellType.ENDER);

        setAttribute(COST, 20);
        setAttribute(COOLDOWN, 15 * Ticks.TICKS_PER_SECOND);

        setAttribute(RANGE, 64d);
    }

    @Override
    protected String rawDescription() {
        return "Teleports you to a random safe space, ";
    }

    @Override
    public void cast(CastContext context) {
        Player player = context.player();
        World world = player.getWorld();

        double range = context.instance().getAttribute(RANGE);

        boolean teleported = EntityUtil.tryRandomTeleport(player, range);

        if (!teleported) {
            player.sendActionBar(Component.text("No safe locations!").color(NamedTextColor.RED));
        }
    }
}
