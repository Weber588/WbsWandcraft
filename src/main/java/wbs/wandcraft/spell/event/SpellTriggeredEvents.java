package wbs.wandcraft.spell.event;

import org.bukkit.util.RayTraceResult;
import wbs.wandcraft.WbsWandcraft;

public class SpellTriggeredEvents {
    public static final SpellTriggeredEvent<RayTraceResult> ON_HIT_TRIGGER = new SpellTriggeredEvent<>(WbsWandcraft.getKey("on_hit"), RayTraceResult.class);
}
