package wbs.wandcraft.context;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.wand.Wand;

import java.util.LinkedList;
import java.util.Queue;

public class CastingQueue {
    private final Queue<SpellInstance> instances;
    @Nullable
    private CastContext current;
    @Nullable
    private final Wand wand;

    public CastingQueue(Queue<SpellInstance> instances, @Nullable Wand wand) {
        this.instances = instances;
        this.wand = wand;
    }

    public @Nullable CastContext getCurrent() {
        return current;
    }

    public Queue<SpellInstance> getInstances() {
        return new LinkedList<>(instances);
    }

    public void startCasting(@NotNull Player player) {
        CastingManager.setCasting(player, this);
        enqueueCast(player);
    }

    private void enqueueCast(@NotNull Player player) {
        if (wand != null) {
            Wand check = Wand.getIfValid(player.getInventory().getItemInMainHand());
            if (check == null || !check.getUUID().equals(wand.getUUID())) {
                CastingManager.stopCasting(player);
                return;
            }
        }

        if (player.isDead() || !player.isOnline()) {
            CastingManager.stopCasting(player);
            return;
        }

        SpellInstance toCast = instances.poll();

        if (toCast == null) {
            CastingManager.stopCasting(player);
            return;
        }

        int delay = Math.max(0, toCast.getAttribute(CastableSpell.DELAY));

        current = toCast.cast(player, () ->
                WbsWandcraft.getInstance().runLater(() -> enqueueCast(player), delay)
        );
    }
}