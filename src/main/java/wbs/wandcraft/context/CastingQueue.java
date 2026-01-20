package wbs.wandcraft.context;

import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.wand.Wand;

import java.util.LinkedList;
import java.util.Queue;

public class CastingQueue {
    private static <T> Queue<T> singletonQueue(T value) {
        LinkedList<T> queue = new LinkedList<>();
        queue.add(value);
        return queue;
    }

    private final Queue<SpellInstance> instances;
    @Nullable
    private CastContext current;
    @Nullable
    private final Wand wand;

    public CastingQueue(SpellInstance instance, @Nullable Wand wand) {
        this(singletonQueue(instance), wand);
    }
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
        EquipmentSlot slot = EquipmentSlot.HAND;
//        if (wand != null) {
//            Wand check = Wand.fromItem(player.getInventory().getItemInMainHand());
//            if (check == null || !check.getUUID().equals(wand.getUUID())) {
//                check = Wand.fromItem(player.getInventory().getItemInOffHand());
//                slot = EquipmentSlot.OFF_HAND;
//            }
//
//            if (check == null || !check.getUUID().equals(wand.getUUID())) {
//                CastingManager.stopCasting(player);
//                return;
//            }
//        }

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

        current = toCast.cast(player, wand, slot, () ->
                WbsWandcraft.getInstance().runLater(() -> enqueueCast(player), delay)
        );
    }
}