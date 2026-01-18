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
import wbs.wandcraft.spell.definitions.extensions.DamageSpell;

import java.util.List;
import java.util.Map;

public class WarlockHat extends MagicHat {
    private static final SpellAttributeModifier<Double, Double> DAMAGE_MODIFIER = DamageSpell.DAMAGE.createModifier(
            AttributeModifierType.MULTIPLY,
            RegisteredPersistentDataType.DOUBLE,
            2.5
    );

    public WarlockHat() {
        super("warlock", HatModel.WARLOCK);
    }

    @Override
    protected @NotNull TextColor getNameColour() {
        return TextColor.color(0x2D2C3B);
    }

    @Override
    public @NotNull List<Component> getEffectsLore() {
        return List.of(
                Component.text("+150% Damage").color(TextColor.color(NamedTextColor.AQUA))
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
                    instance.applyModifier(DAMAGE_MODIFIER);
                }
            }
        }
    }
}
