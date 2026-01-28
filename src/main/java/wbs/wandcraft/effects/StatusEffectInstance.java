package wbs.wandcraft.effects;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.util.persistent.CustomPersistentDataTypes;

import java.util.UUID;

public class StatusEffectInstance {
    public static final NamespacedKey EFFECTS_KEY = WbsWandcraft.getKey("effects");

    public static StatusEffectInstance applyEffect(LivingEntity entity, StatusEffect effect, int duration, boolean showBossBar, LivingEntity cause) {
        StatusEffectInstance existing = StatusEffectManager.getInstance(entity, effect);

        if (existing != null) {
            existing.setRemainingTime(duration);
            existing.setShowBar(showBossBar);
            return existing;
        }

        StatusEffectInstance newInstance = new StatusEffectInstance(effect, duration, showBossBar, cause.getUniqueId());

        newInstance.start(entity);

        return newInstance;
    }

    @Nullable
    private final UUID cause;
    private LivingEntity entity;
    private final StatusEffect effect;
    private int initialTime;
    private int timeLeft;

    private BossBar bar;
    private boolean showBossBar;

    private int taskId = -1;

    public StatusEffectInstance(StatusEffect effect, int duration, boolean initialShowBossBar, @Nullable UUID cause) {
        this.effect = effect;
        this.initialTime = duration;
        this.timeLeft = duration;
        this.showBossBar = initialShowBossBar;
        this.cause = cause;
    }

    public int start(LivingEntity entity) {
        if (taskId != -1) {
            throw new IllegalStateException("Task is already running!");
        }

        StatusEffectManager.trackInstance(entity, this);

        this.entity = entity;

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

        PersistentDataContainer container = entity.getPersistentDataContainer();
        PersistentDataContainer effectsContainer = container.getOrDefault(
                EFFECTS_KEY,
                PersistentDataType.TAG_CONTAINER,
                container.getAdapterContext().newPersistentDataContainer()
        );
        effectsContainer.set(effect.getKey(), CustomPersistentDataTypes.STATUS_EFFECT, this);
        container.set(EFFECTS_KEY, PersistentDataType.TAG_CONTAINER, effectsContainer);
        effect.applyTo(entity, this);

        taskId = new BukkitRunnable() {
            @Override
            public void run() {
                Entity updatedEntity = Bukkit.getEntity(entity.getUniqueId());

                if (entity instanceof Player && !Bukkit.getOfflinePlayer(entity.getUniqueId()).isOnline()) {
                    StatusEffectInstance.this.cancel(false);
                    return;
                }

                if (updatedEntity == null) {
                    StatusEffectInstance.this.cancel(true);
                    return;
                }

                PersistentDataContainer container = entity.getPersistentDataContainer();
                PersistentDataContainer effectsContainer = container.getOrDefault(
                        EFFECTS_KEY,
                        PersistentDataType.TAG_CONTAINER,
                        container.getAdapterContext().newPersistentDataContainer()
                );;

                if (updatedEntity.isDead() || !effect.isValid(entity, StatusEffectInstance.this)) {
                    StatusEffectInstance.this.cancel(true);
                    return;
                }

                timeLeft--;
                if (effectsContainer.has(effect.getKey()) && timeLeft > 0) {
                    effectsContainer.set(effect.getKey(), CustomPersistentDataTypes.STATUS_EFFECT, StatusEffectInstance.this);
                    container.set(EFFECTS_KEY, PersistentDataType.TAG_CONTAINER, effectsContainer);

                    boolean cancel = effect.tick(entity, StatusEffectInstance.this);
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
            }
        }.runTaskTimer(WbsWandcraft.getInstance(), 0L, 1L).getTaskId();

        return taskId;
    }

    public StatusEffect getEffect() {
        return effect;
    }

    public void cancel(boolean removeFromEntity) {
        effect.removeFrom(entity, this);
        StatusEffectManager.stopTracking(entity, this);
        Bukkit.getScheduler().cancelTask(taskId);

        // Don't update the player, as if they're offline, we won't be able to edit persistent data container
        if (entity instanceof Player player) {
            bar.removeViewer(player);
            if (removeFromEntity) {
                removeFromEntity(player);
            }
        }

        Entity updatedEntity = Bukkit.getEntity(entity.getUniqueId());
        if (updatedEntity != null) {
            if (removeFromEntity) {
                removeFromEntity(updatedEntity);
            }
        }
    }

    private void removeFromEntity(Entity updatedEntity) {
        PersistentDataContainer container = updatedEntity.getPersistentDataContainer();

        PersistentDataContainer effectsContainer = container.get(EFFECTS_KEY, PersistentDataType.TAG_CONTAINER);
        if (effectsContainer != null) {
            effectsContainer.remove(effect.getKey());
            if (effectsContainer.isEmpty()) {
                container.remove(EFFECTS_KEY);
            } else {
                container.set(EFFECTS_KEY, PersistentDataType.TAG_CONTAINER, effectsContainer);
            }
        }
    }

    private void setRemainingTime(int duration) {
        entity.getPersistentDataContainer().set(effect.getKey(), CustomPersistentDataTypes.STATUS_EFFECT, StatusEffectInstance.this);

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

    public int getInitialTime() {
        return initialTime;
    }

    public boolean showBossBar() {
        return showBossBar;
    }

    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
    }

    public @Nullable UUID getCause() {
        return cause;
    }

    public @Nullable Entity getCauseEntity() {
        if (cause != null) {
            return Bukkit.getEntity(cause);
        }
        return null;
    }
}
