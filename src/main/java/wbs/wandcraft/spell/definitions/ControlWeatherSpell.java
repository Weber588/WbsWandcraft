package wbs.wandcraft.spell.definitions;

import org.bukkit.World;
import org.bukkit.entity.Player;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.DurationalSpell;

public class ControlWeatherSpell extends SpellDefinition implements CastableSpell, DurationalSpell {
    public ControlWeatherSpell() {
        super("control_weather");
    }

    @Override
    public String rawDescription() {
        return "Begin a storm, bending the weather to become your weapon.";
    }

    @Override
    public void cast(CastContext context) {
        Player player = context.player();
        World world = player.getWorld();


    }
}
