package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.World;
import org.bukkit.entity.Player;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.attributes.attributable.DurationAttributable;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.MagicDomain;

public class ControlWeatherSpell extends SpellDefinition implements CastableSpell, DurationAttributable {
    public ControlWeatherSpell() {
        super("control_weather");

        addMagicDomain(MagicDomain.NATURE);

        setAttribute(COOLDOWN, 30 * 60 * Ticks.TICKS_PER_SECOND);
        setAttribute(DURATION, 5 * 60 * Ticks.TICKS_PER_SECOND);
    }

    @Override
    protected String rawDescription() {
        return "Intensify the weather, or clear it if it's thundering";
    }

    @Override
    public void cast(CastContext context) {
        Player player = context.player();
        World world = player.getWorld();

        if (!world.hasSkyLight() || world.hasCeiling()) {
            return;
        }

        int ticks = context.instance().getAttribute(DURATION);

        if (world.isThundering()) {
            world.setClearWeatherDuration(ticks);
        } else if (world.isClearWeather()) {
            world.setStorm(true);
            world.setWeatherDuration(ticks);
        } else {
            world.setThundering(true);
            world.setThunderDuration(ticks);
        }
    }
}
