package wbs.wandcraft.spell.event;

import org.bukkit.Location;
import org.bukkit.util.RayTraceResult;
import wbs.wandcraft.WbsWandcraft;

public class SpellTriggeredEvents {
    public static final SpellTriggeredEvent<RayTraceResult> ON_HIT_TRIGGER = new SpellTriggeredEvent<>(WbsWandcraft.getKey("on_hit"), RayTraceResult.class);
    public static final SpellTriggeredEvent<Location> OBJECT_TICK_TRIGGER = new SpellTriggeredEvent<>(WbsWandcraft.getKey("on_tick"), Location.class);
    public static final SpellTriggeredEvent<Location> OBJECT_EXPIRE_TRIGGER = new SpellTriggeredEvent<>(WbsWandcraft.getKey("on_expire"), Location.class);
}
