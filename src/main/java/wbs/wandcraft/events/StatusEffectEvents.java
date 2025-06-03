package wbs.wandcraft.events;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.effects.StatusEffectInstance;

public class StatusEffectEvents implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PersistentDataContainer container = player.getPersistentDataContainer();
        PersistentDataContainer effectsContainer = container.getOrDefault(
                StatusEffectInstance.EFFECTS_KEY,
                PersistentDataType.TAG_CONTAINER,
                container.getAdapterContext().newPersistentDataContainer()
        );

        WandcraftRegistries.STATUS_EFFECTS.stream().forEach(effect -> {
            NamespacedKey effectKey = effect.getKey();
            Integer timeLeft = effectsContainer.get(effectKey, PersistentDataType.INTEGER);

            if (timeLeft != null) {
                if (timeLeft <= 0) {
                    effectsContainer.remove(effectKey);
                } else {
                    // TODO: Track if boss bar is shown in effect instance container
                    StatusEffectInstance.applyEffect(player, effect, timeLeft, true);
                }
            }
        });

        container.set(StatusEffectInstance.EFFECTS_KEY, PersistentDataType.TAG_CONTAINER, effectsContainer);
    }
}
