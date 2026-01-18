package wbs.wandcraft.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.spell.definitions.SpellInstance;

import java.util.Queue;

public class SpendManaEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    @NotNull
    private final Player player;
    private final Queue<SpellInstance> spellList;
    private int cost;
    @Nullable
    private final ItemStack usedItem;
    private boolean isCancelled = false;
    private SpendManaResult result = SpendManaResult.CONTINUE;

    public SpendManaEvent(@NotNull Player player, Queue<SpellInstance> spellList, int cost, @Nullable ItemStack usedItem) {
        this.player = player;
        this.spellList = spellList;
        this.cost = cost;
        this.usedItem = usedItem;
    }

    public Queue<SpellInstance> getSpellList() {
        return spellList;
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public int getCost() {
        return cost;
    }

    public SpendManaEvent setCost(int cost) {
        this.cost = cost;
        return this;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.isCancelled = cancel;
    }

    public SpendManaResult getResult() {
        return result;
    }

    public SpendManaEvent setResult(SpendManaResult result) {
        this.result = result;
        return this;
    }

    public @Nullable ItemStack getUsedItem() {
        return usedItem;
    }

    public enum SpendManaResult {
        BYPASS,
        CONTINUE,
        CANCEL,
        FAIL
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
