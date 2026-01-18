package wbs.wandcraft.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CalculateManaRegenRateEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Player player;
    private int manaRegenerationRate;

    public CalculateManaRegenRateEvent(Player player, int defaultManaRegenerationRate) {
        this.player = player;
        this.manaRegenerationRate = defaultManaRegenerationRate;
    }

    public int getManaRegenerationRate() {
        return manaRegenerationRate;
    }

    public CalculateManaRegenRateEvent setManaRegenerationRate(int manaRegenerationRate) {
        this.manaRegenerationRate = manaRegenerationRate;
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
