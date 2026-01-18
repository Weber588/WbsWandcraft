package wbs.wandcraft.equipment.hat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.WbsEventUtils;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.equipment.EquipmentManager;
import wbs.wandcraft.equipment.MagicEquipmentSlot;
import wbs.wandcraft.equipment.MagicEquipmentType;
import wbs.wandcraft.events.CalculateManaRegenCooloffEvent;

import java.util.List;
import java.util.Map;

public class WitchHat extends MagicHat {
    public static final double COOLOFF_REDUCTION = 0.75;

    public WitchHat() {
        super("witch", HatModel.WITCH);
    }

    @Override
    protected @NotNull TextColor getNameColour() {
        return TextColor.color(0x0C8B1A);
    }

    @Override
    public @NotNull List<Component> getEffectsLore() {
        return List.of(
                Component.text("-75% Mana Cooloff").color(TextColor.color(NamedTextColor.AQUA))
        );
    }

    @Override
    public void registerEvents() {
        WbsEventUtils.register(WbsWandcraft.getInstance(), CalculateManaRegenCooloffEvent.class, this::onCalculateCooloff);
    }

    private void onCalculateCooloff(CalculateManaRegenCooloffEvent event) {
        Map<MagicEquipmentSlot, MagicEquipmentType> magicEquipment = EquipmentManager.getMagicEquipment(event.getPlayer());
        for (MagicEquipmentType magicEquipmentType : magicEquipment.values()) {
            if (magicEquipmentType == this) {
                event.setCooloff((int) (event.getCooloff() * (1 - COOLOFF_REDUCTION)));
            }
        }
    }

    @Override
    protected @Nullable String getCredit() {
        return null;
    }
}
