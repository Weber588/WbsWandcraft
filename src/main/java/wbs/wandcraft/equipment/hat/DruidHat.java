package wbs.wandcraft.equipment.hat;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.keys.tags.DamageTypeTagKeys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEventUtils;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.equipment.EquipmentManager;
import wbs.wandcraft.equipment.MagicEquipmentSlot;
import wbs.wandcraft.equipment.MagicEquipmentType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DruidHat extends MagicHat {
    private static final double MAGIC_DAMAGE_REDUCTION = 0.8;
    private static final Set<DamageType> MAGIC_DAMAGE_TYPES = new HashSet<>();

    public DruidHat() {
        super("druid", HatModel.DRUID);

        MAGIC_DAMAGE_TYPES.addAll(
                RegistryAccess.registryAccess()
                        .getRegistry(RegistryKey.DAMAGE_TYPE)
                        .getTagValues(DamageTypeTagKeys.WITCH_RESISTANT_TO)
        );
    }

    @Override
    protected @NotNull TextColor getNameColour() {
        return TextColor.color(0x402F24);
    }

    @Override
    public @NotNull List<Component> getEffectsLore() {
        return List.of(
                Component.text("+80% Magic Resistance").color(TextColor.color(NamedTextColor.AQUA))
        );
    }

    @Override
    public void registerEvents() {
        WbsEventUtils.register(WbsWandcraft.getInstance(), EntityDamageEvent.class, this::onDamage);
    }

    private void onDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
            Map<MagicEquipmentSlot, MagicEquipmentType> magicEquipment = EquipmentManager.getMagicEquipment(player);
            for (MagicEquipmentType magicEquipmentType : magicEquipment.values()) {
                if (magicEquipmentType == this) {
                    @NotNull DamageType damageType = event.getDamageSource().getDamageType();
                    if (MAGIC_DAMAGE_TYPES.contains(damageType)) {
                        event.setDamage(event.getDamage() * (1 - MAGIC_DAMAGE_REDUCTION));
                    }
                }
            }
        }
    }
}
