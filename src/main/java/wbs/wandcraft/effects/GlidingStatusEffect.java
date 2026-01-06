package wbs.wandcraft.effects;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsEventUtils;
import wbs.wandcraft.WbsWandcraft;

@NullMarked
public class GlidingStatusEffect extends StatusEffect {
    @Override
    public boolean onTick(LivingEntity entity, StatusEffectInstance instance) {
        entity.setGliding(true);
        return false;
    }

    @Override
    public boolean isValid(LivingEntity entity, StatusEffectInstance instance) {
        return !entity.isOnGround() && entity.getLocation().add(0, -Double.MIN_VALUE, 0).getBlock().isPassable();
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return WbsWandcraft.getKey("gliding");
    }

    @Override
    public void registerEvents() {
        WbsEventUtils.register(WbsWandcraft.getInstance(), EntityToggleGlideEvent.class, this::onStopGliding);
        WbsEventUtils.register(WbsWandcraft.getInstance(), PlayerKickEvent.class, this::onKick);
    }

    private void onStopGliding(EntityToggleGlideEvent event) {
        if (event.isGliding()) {
            return;
        }

        if (!(event.getEntity() instanceof LivingEntity entity)) {
            return;
        }

        if (entity.isOnGround()) {
            return;
        }

        StatusEffectInstance instance = StatusEffectManager.getInstance(entity, this);

        if (instance != null) {
            event.setCancelled(true);
        }
    }

    private void onKick(PlayerKickEvent event) {
        if (event.getCause() != PlayerKickEvent.Cause.FLYING_PLAYER) {
            return;
        }

        Player player = event.getPlayer();

        if (player.getPersistentDataContainer().has(getKey())) {
            event.setCancelled(true);
        }
    }
}
