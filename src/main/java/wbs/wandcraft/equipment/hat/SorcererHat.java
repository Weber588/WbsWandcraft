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
import wbs.wandcraft.spell.definitions.extensions.DurationalSpell;
import wbs.wandcraft.spell.definitions.extensions.RangedSpell;

import java.util.List;
import java.util.Map;

public class SorcererHat extends MagicHat {
    private static final SpellAttributeModifier<Integer, Double> DURATION_MODIFIER = DurationalSpell.DURATION.createModifier(
            AttributeModifierType.MULTIPLY,
            RegisteredPersistentDataType.DOUBLE,
            1.75
    );
    private static final SpellAttributeModifier<Double, Double> RANGE_MODIFIER = RangedSpell.RANGE.createModifier(
            AttributeModifierType.MULTIPLY,
            RegisteredPersistentDataType.DOUBLE,
            1.25
    );

    public SorcererHat() {
        super("sorcerer", HatModel.SORCERER);
    }

    @Override
    protected @NotNull TextColor getNameColour() {
        return TextColor.color(0x1077AE);
    }

    @Override
    public @NotNull List<Component> getEffectsLore() {
        return List.of(
                Component.text("+75% Spell Duration").color(TextColor.color(NamedTextColor.AQUA)),
                Component.text("+50% Range").color(TextColor.color(NamedTextColor.AQUA))
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
                for (SpellInstance instance : event.getSpellList()) {
                    instance.applyModifier(DURATION_MODIFIER);
                    instance.applyModifier(RANGE_MODIFIER);
                }
            }
        }
    }
}
