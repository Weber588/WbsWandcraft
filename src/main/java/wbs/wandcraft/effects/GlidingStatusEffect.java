package wbs.wandcraft.effects;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsEventUtils;
import wbs.wandcraft.WbsWandcraft;

@NullMarked
public class GlidingStatusEffect implements StatusEffect {
    @Override
    public Component display() {
        return Component.text("Gliding").color(NamedTextColor.GOLD);
    }

    @Override
    public BossBar.Color barColour() {
        return BossBar.Color.YELLOW;
    }

    @Override
    public boolean tick(Player player, int timeLeft) {
        player.setGliding(true);
        return false;
    }

    @Override
    public boolean isValid(Player player) {
        //noinspection deprecation
        return !player.isOnGround() && player.getLocation().add(0, -Double.MIN_VALUE, 0).getBlock().isPassable();
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

        Entity entity = event.getEntity();

        if (entity.isOnGround()) {
            return;
        }

        PersistentDataContainer container = entity.getPersistentDataContainer();
        Integer timeLeft = container.get(getKey(), PersistentDataType.INTEGER);

        if (timeLeft != null) {
            if (timeLeft <= 0) {
                container.remove(getKey());
            } else {
                event.setCancelled(true);
            }
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
