package wbs.wandcraft.effects;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.wandcraft.WbsWandcraft;

public class StatusEffectInstance {
    public static final NamespacedKey EFFECTS_KEY = WbsWandcraft.getKey("effects");
    private final int taskId;

    public static StatusEffectInstance applyEffect(Player player, StatusEffect effect, int duration, boolean showBossBar) {
        StatusEffectInstance existing = StatusEffectManager.getInstance(player, effect);

        if (existing != null) {
            existing.setRemainingTime(duration);
            existing.setShowBar(showBossBar);
            return existing;
        }

        StatusEffectInstance newInstance = new StatusEffectInstance(player, effect, duration, showBossBar);
        StatusEffectManager.trackInstance(player, newInstance);

        return newInstance;
    }

    private final BossBar bar;

    private int initialTime;
    private final Player player;

    private final StatusEffect effect;
    private int timeLeft;
    private boolean showBossBar;
    private StatusEffectInstance(Player player, StatusEffect effect, int duration, boolean initialShowBossBar) {
        this.player = player;
        this.effect = effect;
        this.initialTime = duration;
        this.timeLeft = duration;
        this.showBossBar = initialShowBossBar;

        Component barName = effect.display();
        bar = BossBar.bossBar(
                barName,
                1.0f,
                effect.barColour(),
                effect.barStyle()
        );

        if (showBossBar) {
            bar.addViewer(player);
        }

        taskId = startTimer(player, effect);
    }

    private int startTimer(Player player, StatusEffect effect) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        PersistentDataContainer effectsContainer = container.getOrDefault(
                EFFECTS_KEY,
                PersistentDataType.TAG_CONTAINER,
                container.getAdapterContext().newPersistentDataContainer()
        );
        effectsContainer.set(effect.getKey(), PersistentDataType.INTEGER, initialTime);
        container.set(EFFECTS_KEY, PersistentDataType.TAG_CONTAINER, effectsContainer);

        return new BukkitRunnable() {
            @Override
            public void run() {
                Player updatedPlayer = Bukkit.getPlayer(player.getUniqueId());

                if (updatedPlayer == null) {
                    StatusEffectInstance.this.cancel(false);
                    return;
                }

                if (!updatedPlayer.isOnline()) {
                    StatusEffectInstance.this.cancel(false);
                    return;
                }

                PersistentDataContainer container = player.getPersistentDataContainer();
                PersistentDataContainer effectsContainer = container.getOrDefault(
                        EFFECTS_KEY,
                        PersistentDataType.TAG_CONTAINER,
                        container.getAdapterContext().newPersistentDataContainer()
                );;

                if (updatedPlayer.isDead() || !effect.isValid(player)) {
                    StatusEffectInstance.this.cancel(true);
                    return;
                }

                timeLeft--;
                if (effectsContainer.has(effect.getKey()) && timeLeft > 0) {
                    effectsContainer.set(effect.getKey(), PersistentDataType.INTEGER, timeLeft);

                    boolean cancel = effect.tick(player, timeLeft);
                    if (cancel) {
                        StatusEffectInstance.this.cancel(true);
                    }

                    bar.progress(timeLeft / (float) initialTime);

                    if (showBossBar) {
                        bar.addViewer(player);
                    } else {
                        bar.removeViewer(player);
                    }
                } else {
                    StatusEffectInstance.this.cancel(true);
                }

                container.set(EFFECTS_KEY, PersistentDataType.TAG_CONTAINER, effectsContainer);
            }
        }.runTaskTimer(WbsWandcraft.getInstance(), 0L, 1L).getTaskId();
    }

    public StatusEffect getEffect() {
        return effect;
    }

    public void cancel(boolean removeFromPlayer) {
        StatusEffectManager.stopTracking(player, StatusEffectInstance.this);
        Bukkit.getScheduler().cancelTask(taskId);

        Player updatedPlayer = Bukkit.getPlayer(player.getUniqueId());
        if (updatedPlayer != null) {
            bar.removeViewer(updatedPlayer);

            if (removeFromPlayer) {
                PersistentDataContainer container = updatedPlayer.getPersistentDataContainer();

                PersistentDataContainer effectsContainer = container.get(EFFECTS_KEY, PersistentDataType.TAG_CONTAINER);
                if (effectsContainer != null) {
                    effectsContainer.remove(effect.getKey());
                    container.set(EFFECTS_KEY, PersistentDataType.TAG_CONTAINER, effectsContainer);
                }
            }
        }
    }

    private void setRemainingTime(int duration) {
        player.getPersistentDataContainer().set(effect.getKey(), PersistentDataType.INTEGER, duration);

        timeLeft = duration;
        if (initialTime > timeLeft) {
            initialTime = timeLeft;
        }
    }

    private void setShowBar(boolean showBossBar) {
        this.showBossBar = showBossBar;
    }
}
