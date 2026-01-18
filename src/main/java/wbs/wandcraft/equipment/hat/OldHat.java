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
import wbs.wandcraft.events.CalculateManaRegenCooloffEvent;
import wbs.wandcraft.events.CalculateMaxManaEvent;
import wbs.wandcraft.events.EnqueueSpellsEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OldHat extends MagicHat {
    private static final double COOLOFF_REDUCTION = 0.1;
    private static final double MANA_INCREASE = 0.1;
    private static final double COOLDOWN_REDUCTION = 0.1;
    private static final double MAGIC_DAMAGE_REDUCTION = 0.1;
    private static final Set<DamageType> MAGIC_DAMAGE_TYPES = new HashSet<>();

    public OldHat() {
        super("old", HatModel.OLD);

        MAGIC_DAMAGE_TYPES.addAll(
                RegistryAccess.registryAccess()
                        .getRegistry(RegistryKey.DAMAGE_TYPE)
                        .getTagValues(DamageTypeTagKeys.WITCH_RESISTANT_TO)
        );
    }

    @Override
    protected @NotNull String getName() {
        return "Old Hat";
    }

    @Override
    protected @NotNull TextColor getNameColour() {
        return TextColor.color(0x80591A);
    }

    @Override
    public @NotNull List<Component> getEffectsLore() {
        return List.of(
                Component.text("-10% Mana Cooloff").color(TextColor.color(NamedTextColor.AQUA)),
                Component.text("+10% Max Mana").color(TextColor.color(NamedTextColor.AQUA)),
                Component.text("-10% Cooldown").color(TextColor.color(NamedTextColor.AQUA)),
                Component.text("+10% Magic Resistance").color(TextColor.color(NamedTextColor.AQUA))
        );
    }

    @Override
    public void registerEvents() {
        WbsEventUtils.register(WbsWandcraft.getInstance(), EnqueueSpellsEvent.class, this::onEnqueueSpells);
        WbsEventUtils.register(WbsWandcraft.getInstance(), CalculateMaxManaEvent.class, this::onCalculateMaxMana);
        WbsEventUtils.register(WbsWandcraft.getInstance(), CalculateManaRegenCooloffEvent.class, this::onCalculateCooloff);
        WbsEventUtils.register(WbsWandcraft.getInstance(), EntityDamageEvent.class, this::onDamage);
    }

    private void onEnqueueSpells(EnqueueSpellsEvent event) {
        Map<MagicEquipmentSlot, MagicEquipmentType> magicEquipment = EquipmentManager.getMagicEquipment(event.getPlayer());
        for (MagicEquipmentType magicEquipmentType : magicEquipment.values()) {
            if (magicEquipmentType == this) {
                event.setAdditionalCooldown((int) (event.getAdditionalCooldown() * (1 - COOLDOWN_REDUCTION)));
            }
        }
    }

    private void onCalculateMaxMana(CalculateMaxManaEvent event) {
        Map<MagicEquipmentSlot, MagicEquipmentType> magicEquipment = EquipmentManager.getMagicEquipment(event.getPlayer());
        for (MagicEquipmentType magicEquipmentType : magicEquipment.values()) {
            if (magicEquipmentType == this) {
                event.setMaxMana((int) (event.getMaxMana() * (1 + MANA_INCREASE)));
            }
        }
    }

    private void onCalculateCooloff(CalculateManaRegenCooloffEvent event) {
        Map<MagicEquipmentSlot, MagicEquipmentType> magicEquipment = EquipmentManager.getMagicEquipment(event.getPlayer());
        for (MagicEquipmentType magicEquipmentType : magicEquipment.values()) {
            if (magicEquipmentType == this) {
                event.setCooloff((int) (event.getCooloff() * (1 - COOLOFF_REDUCTION)));
            }
        }
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
