package wbs.wandcraft.effects;

import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityTargetEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsEventUtils;
import wbs.wandcraft.WbsWandcraft;

import java.util.Map;

@NullMarked
public class PacifiedEffect extends StatusEffect {
    private final AttributeModifier damageModifier = new AttributeModifier(getKey(), -1, AttributeModifier.Operation.MULTIPLY_SCALAR_1);

    @Override
    public BossBar.Color barColour() {
        return BossBar.Color.BLUE;
    }

    @Override
    public Map<Attribute, AttributeModifier> getAttributes() {
        return Map.of(
                Attribute.ATTACK_DAMAGE, damageModifier
        );
    }

    @Override
    public void onApply(LivingEntity entity, StatusEffectInstance instance) {
        if (entity instanceof Player targetPlayer) {
            WbsWandcraft.getInstance().sendActionBar("Pacified!", targetPlayer);
        }
    }

    @Override
    public void registerEvents() {
        WbsEventUtils.register(WbsWandcraft.getInstance(), EntityTargetEvent.class, this::onTarget);
    }

    private void onTarget(EntityTargetEvent event) {
        if (event.getEntity() instanceof Mob mob && mob instanceof Enemy) {
            ifPresent(mob, instance -> {
                event.setCancelled(true);
            });
        }
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return WbsWandcraft.getKey("tranquilized");
    }
}
