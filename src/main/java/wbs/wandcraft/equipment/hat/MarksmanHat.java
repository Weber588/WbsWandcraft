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
import wbs.wandcraft.spell.definitions.extensions.DirectionalSpell;
import wbs.wandcraft.spell.definitions.extensions.RangedSpell;

import java.util.List;
import java.util.Map;

public class MarksmanHat extends MagicHat {
    private static final SpellAttributeModifier<Double, Double> IMPRECISION_MODIFIER = DirectionalSpell.IMPRECISION.createModifier(
            AttributeModifierType.MULTIPLY,
            RegisteredPersistentDataType.DOUBLE,
            0d
    );
    private static final SpellAttributeModifier<Double, Double> RANGE_MODIFIER = RangedSpell.RANGE.createModifier(
            AttributeModifierType.MULTIPLY,
            RegisteredPersistentDataType.DOUBLE,
            1.5
    );

    public MarksmanHat() {
        super("marksman", HatModel.MARKSMAN);
    }

    @Override
    protected @NotNull TextColor getNameColour() {
        return TextColor.color(0x2E4805);
    }

    @Override
    public @NotNull List<Component> getEffectsLore() {
        return List.of(
                Component.text("-100% Imprecision").color(TextColor.color(NamedTextColor.AQUA)),
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
                    instance.applyModifier(IMPRECISION_MODIFIER);
                    instance.applyModifier(RANGE_MODIFIER);
                }
            }
        }
    }
}
