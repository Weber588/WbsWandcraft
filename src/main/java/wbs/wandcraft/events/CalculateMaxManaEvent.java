package wbs.wandcraft.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CalculateMaxManaEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private int maxMana;

    public CalculateMaxManaEvent(Player player, int defaultMaxMana) {
        this.player = player;
        this.maxMana = defaultMaxMana;
    }

    public Player getPlayer() {
        return player;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public CalculateMaxManaEvent setMaxMana(int maxMana) {
        this.maxMana = maxMana;
        return this;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
