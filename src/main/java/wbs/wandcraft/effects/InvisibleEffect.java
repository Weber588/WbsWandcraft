package wbs.wandcraft.effects;

import net.kyori.adventure.util.Ticks;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsEventUtils;
import wbs.wandcraft.WbsWandcraft;

import java.util.Set;

/**
 * A better version of invisibility that hides particles and prevents mob tracking.
 */
@NullMarked
public class InvisibleEffect extends StatusEffect {
    @Override
    public Set<PotionEffect> getPotionEffects() {
        return Set.of(new PotionEffect(PotionEffectType.INVISIBILITY, Ticks.TICKS_PER_SECOND, 0, false, false, true));
    }

    @Override
    protected void onApply(LivingEntity entity, StatusEffectInstance instance) {
        entity.setArrowsInBody(0);
    }

    @Override
    public void registerEvents() {
        super.registerEvents();

        WbsEventUtils.register(WbsWandcraft.getInstance(), EntityTargetEvent.class, this::onMobTarget);
        WbsEventUtils.register(WbsWandcraft.getInstance(), EntityDamageByEntityEvent.class, this::onAttack);
    }

    private void onAttack(EntityDamageByEntityEvent event) {
        ifPresent(event.getDamager(), instance -> instance.cancel(true));
    }

    private void onMobTarget(EntityTargetEvent event) {
        ifPresent(event.getTarget(), instance -> event.setCancelled(true));
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return WbsWandcraft.getKey("invisible");
    }
}
