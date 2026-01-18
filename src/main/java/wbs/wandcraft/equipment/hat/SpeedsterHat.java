package wbs.wandcraft.equipment.hat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEventUtils;
import wbs.wandcraft.RegisteredPersistentDataType;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.equipment.EquipmentManager;
import wbs.wandcraft.equipment.MagicEquipmentSlot;
import wbs.wandcraft.equipment.MagicEquipmentType;
import wbs.wandcraft.events.EnqueueSpellsEvent;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.extensions.SpeedSpell;

import java.util.List;
import java.util.Map;

public class SpeedsterHat extends MagicHat {
    private static final SpellAttributeModifier<Double, Double> SPEED_MODIFIER = SpeedSpell.SPEED.createModifier(
            AttributeModifierType.MULTIPLY,
            RegisteredPersistentDataType.DOUBLE,
            1.5
    );
    private static final double COOLDOWN_REDUCTION = 0.25;

    public SpeedsterHat() {
        super("speedster", HatModel.SPEEDSTER);
    }

    @Override
    protected @NotNull TextColor getNameColour() {
        return TextColor.color(0x563C76);
    }

    @Override
    public @NotNull List<Component> getEffectsLore() {
        return List.of(
                Component.text("+50% Speed").color(TextColor.color(NamedTextColor.AQUA)),
                Component.text("-25% Cooldown").color(TextColor.color(NamedTextColor.AQUA))
        );
    }

    @Override
    public void registerEvents() {
        WbsEventUtils.register(WbsWandcraft.getInstance(), EnqueueSpellsEvent.class, this::onEnqueueSpells);
    }

    private void onEnqueueSpells(EnqueueSpellsEvent event) {
        Map<MagicEquipmentSlot, MagicEquipmentType> magicEquipment = EquipmentManager.getMagicEquipment(event.getPlayer());
        for (MagicEquipmentType magicEquipmentType : magicEquipment.values()) {
            if (magicEquipmentType == this) {
                event.setAdditionalCooldown((int) (event.getAdditionalCooldown() * (1 - COOLDOWN_REDUCTION)));

                for (SpellInstance instance : event.getSpellList()) {
                    instance.applyModifier(SPEED_MODIFIER);
                }
            }
        }
    }
}
