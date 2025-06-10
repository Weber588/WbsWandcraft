package wbs.wandcraft.effects;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.WbsWandcraft;

@NullMarked
public class StunnedEffect implements StatusEffect {
    @Override
    public Component display() {
        return Component.text("Stunned").color(NamedTextColor.YELLOW);
    }

    @Override
    public boolean tick(LivingEntity entity, int timeLeft) {
        ItemStack activeItem = entity.getActiveItem();
        if (!activeItem.isEmpty()) {
            entity.clearActiveItem();
        }

        if (entity instanceof Player player) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (!item.isEmpty() && player.getCooldown(item) == 0) {
                player.setCooldown(item, timeLeft);
            }
        } else if (entity instanceof Mob mob) {
            mob.setTarget(null);
        }
        return false;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return WbsWandcraft.getKey("stunned");
    }
}
