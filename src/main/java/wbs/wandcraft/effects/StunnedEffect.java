package wbs.wandcraft.effects;

import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.WbsWandcraft;

@NullMarked
public class StunnedEffect extends StatusEffect {
    @Override
    public boolean onTick(LivingEntity entity, StatusEffectInstance instance) {
        ItemStack activeItem = entity.getActiveItem();
        if (!activeItem.isEmpty()) {
            entity.clearActiveItem();
        }

        if (entity instanceof Player player) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (!item.isEmpty() && player.getCooldown(item) == 0) {
                player.setCooldown(item, instance.getTimeLeft());
            }
        } else if (entity instanceof Mob mob) {
            mob.setTarget(null);
        }
        return false;
    }

    @Override
    public void onApply(LivingEntity entity, StatusEffectInstance instance) {
        entity.getWorld().spawnParticle(Particle.FLASH, entity.getEyeLocation(), 0);
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return WbsWandcraft.getKey("stunned");
    }
}
