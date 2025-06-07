package wbs.wandcraft.effects;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.wandcraft.WbsWandcraft;

public class StatusEffectInstance {
    public static final NamespacedKey EFFECTS_KEY = WbsWandcraft.getKey("effects");
    private final int taskId;

    public static StatusEffectInstance applyEffect(LivingEntity entity, StatusEffect effect, int duration, boolean showBossBar) {
        StatusEffectInstance existing = StatusEffectManager.getInstance(entity, effect);

        if (existing != null) {
            existing.setRemainingTime(duration);
            existing.setShowBar(showBossBar);
            return existing;
        }

        StatusEffectInstance newInstance = new StatusEffectInstance(entity, effect, duration, showBossBar);
        StatusEffectManager.trackInstance(entity, newInstance);

        return newInstance;
    }

    private final BossBar bar;

    private int initialTime;
    private final LivingEntity entity;

    private final StatusEffect effect;
    private int timeLeft;
    private boolean showBossBar;
    private StatusEffectInstance(LivingEntity entity, StatusEffect effect, int duration, boolean initialShowBossBar) {
        this.entity = entity;
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
            bar.addViewer(entity);
        }

        taskId = startTimer(entity, effect);
    }

    private int startTimer(LivingEntity entity, StatusEffect effect) {
        PersistentDataContainer container = entity.getPersistentDataContainer();
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
                Player updatedPlayer = Bukkit.getPlayer(entity.getUniqueId());

                if (updatedPlayer == null) {
                    StatusEffectInstance.this.cancel(false);
                    return;
                }

                if (!updatedPlayer.isOnline()) {
                    StatusEffectInstance.this.cancel(false);
                    return;
                }

                PersistentDataContainer container = entity.getPersistentDataContainer();
                PersistentDataContainer effectsContainer = container.getOrDefault(
                        EFFECTS_KEY,
                        PersistentDataType.TAG_CONTAINER,
                        container.getAdapterContext().newPersistentDataContainer()
                );;

                if (updatedPlayer.isDead() || !effect.isValid(entity)) {
                    StatusEffectInstance.this.cancel(true);
                    return;
                }

                timeLeft--;
                if (effectsContainer.has(effect.getKey()) && timeLeft > 0) {
                    effectsContainer.set(effect.getKey(), PersistentDataType.INTEGER, timeLeft);

                    boolean cancel = effect.tick(entity, timeLeft);
                    if (cancel) {
                        StatusEffectInstance.this.cancel(true);
                    }

                    bar.progress(timeLeft / (float) initialTime);

                    if (showBossBar) {
                        bar.addViewer(entity);
                    } else {
                        bar.removeViewer(entity);
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
        StatusEffectManager.stopTracking(entity, StatusEffectInstance.this);
        Bukkit.getScheduler().cancelTask(taskId);

        Player updatedPlayer = Bukkit.getPlayer(entity.getUniqueId());
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
        entity.getPersistentDataContainer().set(effect.getKey(), PersistentDataType.INTEGER, duration);

        timeLeft = duration;
        if (initialTime > timeLeft) {
            initialTime = timeLeft;
        }
    }

    private void setShowBar(boolean showBossBar) {
        this.showBossBar = showBossBar;
    }

    public int getTimeLeft() {
        return timeLeft;
    }
}
