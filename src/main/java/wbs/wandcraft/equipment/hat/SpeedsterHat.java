package wbs.wandcraft.equipment.hat;

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
import wbs.wandcraft.spell.definitions.extensions.ForceSpell;
import wbs.wandcraft.spell.definitions.extensions.SpeedSpell;

import java.util.List;
import java.util.Map;

public class SpeedsterHat extends MagicHat {
    public static final double SPEED_MULTIPLIER = 1.5;

    private static final SpellAttributeModifier<Double, Double> SPEED_MODIFIER = SpeedSpell.SPEED.createModifier(
            AttributeModifierType.MULTIPLY,
            RegisteredPersistentDataType.DOUBLE,
            SPEED_MULTIPLIER
    );
    private static final SpellAttributeModifier<Double, Double> FORCE_MODIFIER = ForceSpell.FORCE.createModifier(
            AttributeModifierType.MULTIPLY,
            RegisteredPersistentDataType.DOUBLE,
            SPEED_MULTIPLIER
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
    public List<String> getEffectsLore() {
        return List.of(
                "+50% Spell Speed",
                "-25% Spell Cooldown"
        );
    }

    @Override
    public void registerEvents() {
        super.registerEvents();
        WbsEventUtils.register(WbsWandcraft.getInstance(), EnqueueSpellsEvent.class, this::onEnqueueSpells);
    }
    // TODO: Modify vex flying speed
    // TODO: Speed up spell casting goal maybe?
    private void onEnqueueSpells(EnqueueSpellsEvent event) {
        Map<MagicEquipmentSlot, MagicEquipmentType> magicEquipment = EquipmentManager.getMagicEquipment(event.getPlayer());
        for (MagicEquipmentType magicEquipmentType : magicEquipment.values()) {
            if (magicEquipmentType == this) {
                event.setAdditionalCooldown((int) (event.getAdditionalCooldown() * (1 - COOLDOWN_REDUCTION)));

                for (SpellInstance instance : event.getSpellList()) {
                    instance.applyModifier(SPEED_MODIFIER);
                    instance.applyModifier(FORCE_MODIFIER);
                }
            }
        }
    }
}
