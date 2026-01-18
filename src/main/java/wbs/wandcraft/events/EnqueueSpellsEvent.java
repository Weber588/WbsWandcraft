package wbs.wandcraft.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.spell.definitions.SpellInstance;

import java.util.Queue;

public class EnqueueSpellsEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    @NotNull
    private final Player player;
    private final Queue<SpellInstance> spellList;
    private int additionalCooldown;

    public EnqueueSpellsEvent(@NotNull Player player, Queue<SpellInstance> spellList, int additionalCooldown) {
        this.player = player;
        this.spellList = spellList;
        this.additionalCooldown = additionalCooldown;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public Queue<SpellInstance> getSpellList() {
        return spellList;
    }

    public int getAdditionalCooldown() {
        return additionalCooldown;
    }

    public EnqueueSpellsEvent setAdditionalCooldown(int additionalCooldown) {
        this.additionalCooldown = additionalCooldown;
        return this;
    }
}
