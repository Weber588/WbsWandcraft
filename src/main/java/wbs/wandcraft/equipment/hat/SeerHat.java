package wbs.wandcraft.equipment.hat;

import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEventUtils;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.cost.PlayerMana;
import wbs.wandcraft.equipment.EquipmentManager;
import wbs.wandcraft.equipment.MagicEquipmentSlot;
import wbs.wandcraft.equipment.MagicEquipmentType;
import wbs.wandcraft.util.DamageUtils;

import java.util.List;
import java.util.Map;

public class SeerHat extends MagicHat {
    private static final double MANA_INCREASE = 0.5;
    public static final double XP_INCREASE = 0.5;
    private static final double MANA_PER_XP = 5;

    public SeerHat() {
        super("seer", HatModel.SEER);
    }

    @Override
    protected @NotNull TextColor getNameColour() {
        return TextColor.color(0x79D1D6);
    }

    @Override
    public List<String> getEffectsLore() {
        return List.of(
                "+50% XP Drops",
                "XP orbs regenerate your mana"
        );
    }

    @Override
    public void registerEvents() {
        super.registerEvents();
        WbsEventUtils.register(WbsWandcraft.getInstance(), PlayerPickupExperienceEvent.class, this::onXPPickup);
        WbsEventUtils.register(WbsWandcraft.getInstance(), EntityDeathEvent.class, this::onEntityDeath);
    }

    private void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            DamageType damageType = event.getDamageSource().getDamageType();
            if (DamageUtils.isMagicDamage(damageType)) {
                ifEquipped(killer, () -> {
                    event.setDroppedExp((int) (event.getDroppedExp() * (1 + XP_INCREASE)));
                });
            }
        }
    }

    private void onXPPickup(PlayerPickupExperienceEvent event) {
        Player player = event.getPlayer();
        Map<MagicEquipmentSlot, MagicEquipmentType> magicEquipment = EquipmentManager.getMagicEquipment(player);
        for (MagicEquipmentType magicEquipmentType : magicEquipment.values()) {
            if (magicEquipmentType == this) {
                int experience = event.getExperienceOrb().getExperience();

                if (experience > 0) {
                    PlayerMana mana = new PlayerMana(player);
                    if (mana.getMana() < mana.getMaxMana()) {
                        mana.setMana((int) Math.min(mana.getMana() + (experience * MANA_PER_XP), mana.getMaxMana()));
                        mana.saveTo(player);
                        event.getExperienceOrb().remove();
                        event.setCancelled(true);
                    }
                }
            }
        }
    }
}
