package wbs.wandcraft.equipment.hat;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEventUtils;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.util.DamageUtils;

import java.util.List;

public class DruidHat extends MagicHat {
    private static final double MAGIC_DAMAGE_REDUCTION = 0.8;

    public DruidHat() {
        super("druid", HatModel.DRUID);
    }

    @Override
    protected @NotNull TextColor getNameColour() {
        return TextColor.color(0x402F24);
    }

    @Override
    public List<String> getEffectsLore() {
        return List.of(
                "+80% Magic Damage Resistance"
        );
    }

    @Override
    public void registerEvents() {
        super.registerEvents();
        WbsEventUtils.register(WbsWandcraft.getInstance(), EntityDamageEvent.class, this::onDamage);
    }

    private void onDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity livingEntity) {
            @NotNull DamageType damageType = event.getDamageSource().getDamageType();
            if (DamageUtils.isMagicDamage(damageType)) {
                ifEquipped(livingEntity, () -> {
                    event.setDamage(event.getDamage() * (1 - MAGIC_DAMAGE_REDUCTION));
                });
            }
        }
    }
}
