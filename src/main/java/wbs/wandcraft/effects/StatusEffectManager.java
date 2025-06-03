package wbs.wandcraft.effects;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.UUID;

public class StatusEffectManager {
    private static final Table<NamespacedKey, UUID, StatusEffectInstance> EFFECT_INSTANCES = HashBasedTable.create();

    public static StatusEffectInstance getInstance(Player player, StatusEffect effect) {
        return EFFECT_INSTANCES.get(effect.getKey(), player.getUniqueId());
    }

    public static void stopTracking(Player player, StatusEffectInstance instance) {
        EFFECT_INSTANCES.remove(instance.getEffect().getKey(), player.getUniqueId());
    }

    public static void trackInstance(Player player, StatusEffectInstance newInstance) {
        if (EFFECT_INSTANCES.contains(newInstance.getEffect().getKey(), player.getUniqueId())) {
            throw new IllegalStateException("That status effect is already being tracked on that player! Add to the existing time instead.");
        }

        EFFECT_INSTANCES.put(newInstance.getEffect().getKey(), player.getUniqueId(), newInstance);
    }
}
