package wbs.wandcraft.entities;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.wandcraft.WbsWandcraft;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class CustomEntity {
    private static final Map<UUID, Integer> TIMERS = new HashMap<>();

    public CustomEntity(LivingEntity wrapped) {
        UUID uuid = wrapped.getUniqueId();

        if (TIMERS.containsKey(uuid)) {
            return;
        }

        int id = WbsWandcraft.getInstance().runTimer(runnable -> {
            if (runnable.getTaskId() != TIMERS.getOrDefault(uuid, -1)) {
                runnable.cancel();
                TIMERS.remove(uuid);
                return;
            }

            Entity updated = Bukkit.getEntity(uuid);

            if (!(updated instanceof LivingEntity updatedEntity)) {
                runnable.cancel();
                TIMERS.remove(uuid);
                return;
            }

            if (!updatedEntity.isValid()) {
                runnable.cancel();
            } else {
                tick(updatedEntity, runnable);
            }

            if (runnable.isCancelled()) {
                TIMERS.remove(uuid);
            }
        }, 1, 1);

        TIMERS.put(uuid, id);
    }

    protected abstract void tick(LivingEntity updatedEntity, BukkitRunnable runnable);
}
