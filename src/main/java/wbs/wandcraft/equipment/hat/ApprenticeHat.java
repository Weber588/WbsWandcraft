package wbs.wandcraft.equipment.hat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEventUtils;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.equipment.EquipmentManager;
import wbs.wandcraft.equipment.MagicEquipmentSlot;
import wbs.wandcraft.equipment.MagicEquipmentType;
import wbs.wandcraft.events.CalculateMaxManaEvent;

import java.util.List;
import java.util.Map;

public class ApprenticeHat extends MagicHat {
    private static final double MANA_INCREASE = 0.5;

    public ApprenticeHat() {
        super("apprentice", HatModel.APPRENTICE);
    }

    @Override
    protected @NotNull TextColor getNameColour() {
        return TextColor.color(0x0c8d5b);
    }

    @Override
    public @NotNull List<Component> getEffectsLore() {
        return List.of(
                Component.text("+50% Max Mana").color(TextColor.color(NamedTextColor.AQUA))
        );
    }

    @Override
    public void registerEvents() {
        WbsEventUtils.register(WbsWandcraft.getInstance(), CalculateMaxManaEvent.class, this::onCalculateMaxMana);
    }

    private void onCalculateMaxMana(CalculateMaxManaEvent event) {
        Map<MagicEquipmentSlot, MagicEquipmentType> magicEquipment = EquipmentManager.getMagicEquipment(event.getPlayer());
        for (MagicEquipmentType magicEquipmentType : magicEquipment.values()) {
            if (magicEquipmentType == this) {
                event.setMaxMana((int) (event.getMaxMana() * (1 + MANA_INCREASE)));
            }
        }
    }

    // TODO: Catch spell cast event and check nearby players for those wearing this hat. They'll learn the spell.
}
