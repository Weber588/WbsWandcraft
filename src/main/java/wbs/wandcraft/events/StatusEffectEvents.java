package wbs.wandcraft.events;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.event.world.EntitiesUnloadEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.effects.StatusEffectInstance;
import wbs.wandcraft.effects.StatusEffectManager;
import wbs.wandcraft.util.CustomPersistentDataTypes;

public class StatusEffectEvents implements Listener {

    @EventHandler
    public void onEntityUnload(EntitiesUnloadEvent event) {
        event.getEntities().forEach(entity -> {
            if (entity instanceof LivingEntity livingEntity) {
                StatusEffectManager.getInstances(livingEntity).forEach(instance -> instance.cancel(false));
            }
        });

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        StatusEffectManager.getInstances(player).forEach(instance -> instance.cancel(false));
    }

    @EventHandler
    public void onEntityLoad(EntitiesLoadEvent event) {
        event.getEntities().forEach(entity -> {
            if (entity instanceof LivingEntity livingEntity) {
                restartEffect(livingEntity);
            }
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        restartEffect(player);
    }

    private static void restartEffect(LivingEntity entity) {
        PersistentDataContainer container = entity.getPersistentDataContainer();
        PersistentDataContainer effectsContainer = container.getOrDefault(
                StatusEffectInstance.EFFECTS_KEY,
                PersistentDataType.TAG_CONTAINER,
                container.getAdapterContext().newPersistentDataContainer()
        );

        WandcraftRegistries.STATUS_EFFECTS.stream().forEach(effect -> {
            NamespacedKey effectKey = effect.getKey();

            StatusEffectInstance instance = effectsContainer.get(effectKey, CustomPersistentDataTypes.STATUS_EFFECT);

            if (instance != null) {
                if (instance.getTimeLeft() <= 0) {
                    effectsContainer.remove(effectKey);
                } else {
                    if (StatusEffectManager.getInstance(entity, instance.getEffect()) == null) {
                        instance.start(entity);
                    }
                }
            }
        });

        // Update in case any old effects were removed
        container.set(StatusEffectInstance.EFFECTS_KEY, PersistentDataType.TAG_CONTAINER, effectsContainer);
    }
}
