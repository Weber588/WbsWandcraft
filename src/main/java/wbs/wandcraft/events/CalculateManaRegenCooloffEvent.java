package wbs.wandcraft.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CalculateManaRegenCooloffEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private int cooloff;

    public CalculateManaRegenCooloffEvent(Player player, int defaultCooloff) {
        this.player = player;
        this.cooloff = defaultCooloff;
    }

    public int getCooloff() {
        return cooloff;
    }

    public CalculateManaRegenCooloffEvent setCooloff(int cooloff) {
        this.cooloff = cooloff;
        return this;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
