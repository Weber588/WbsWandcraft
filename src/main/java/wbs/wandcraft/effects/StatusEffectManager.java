package wbs.wandcraft.effects;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;

import java.util.Collection;
import java.util.UUID;

public class StatusEffectManager {
    public static final GlidingStatusEffect GLIDING = new GlidingStatusEffect();
    public static final StunnedEffect STUNNED = new StunnedEffect();
    public static final PlanarBindingEffect PLANAR_BINDING = new PlanarBindingEffect();
    public static final DeathWalkEffect DEATH_WALK = new DeathWalkEffect();
    public static final CharmedEffect CHARMED = new CharmedEffect();
    public static final PolymorphedEffect POLYMORPHED = new PolymorphedEffect();
    public static final HoldEffect HOLD = new HoldEffect();

    private static final Table<NamespacedKey, UUID, StatusEffectInstance> EFFECT_INSTANCES = HashBasedTable.create();

    public static StatusEffectInstance getInstance(LivingEntity entity, StatusEffect effect) {
        return EFFECT_INSTANCES.get(effect.getKey(), entity.getUniqueId());
    }

    public static Collection<StatusEffectInstance> getInstances(LivingEntity entity) {
        return EFFECT_INSTANCES.column(entity.getUniqueId()).values();
    }

    public static void stopTracking(LivingEntity entity, StatusEffectInstance instance) {
        EFFECT_INSTANCES.remove(instance.getEffect().getKey(), entity.getUniqueId());
    }

    public static void trackInstance(LivingEntity entity, StatusEffectInstance newInstance) {
        if (EFFECT_INSTANCES.contains(newInstance.getEffect().getKey(), entity.getUniqueId())) {
            throw new IllegalStateException(newInstance.getEffect().getKey() + " is already being tracked on that entity! Add to the existing time instead.");
        }

        EFFECT_INSTANCES.put(newInstance.getEffect().getKey(), entity.getUniqueId(), newInstance);
    }
}
