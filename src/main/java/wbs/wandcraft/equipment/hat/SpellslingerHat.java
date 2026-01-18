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
import wbs.wandcraft.spell.definitions.extensions.CustomProjectileSpell;
import wbs.wandcraft.spell.definitions.extensions.IProjectileSpell;

import java.util.List;
import java.util.Map;

public class SpellslingerHat extends MagicHat {
    private static final SpellAttributeModifier<Double, Double> SPEED_MODIFIER = CustomProjectileSpell.SPEED.createModifier(
            AttributeModifierType.MULTIPLY,
            RegisteredPersistentDataType.DOUBLE,
            1.75
    );
    private static final double PROJECTILE_COOLDOWN_REDUCTION = 0.5;

    public SpellslingerHat() {
        super("spellslinger", HatModel.SPELLSLINGER);
    }

    @Override
    protected @NotNull TextColor getNameColour() {
        return TextColor.color(0x907CC0);
    }

    @Override
    public @NotNull List<Component> getEffectsLore() {
        return List.of(
                Component.text("-50% Projectile Cooldown").color(TextColor.color(NamedTextColor.AQUA)),
                Component.text("+50% Projectile Speed").color(TextColor.color(NamedTextColor.AQUA))
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
                int cooldownToRemove = 0;
                for (SpellInstance instance : event.getSpellList()) {
                    if (instance.getDefinition() instanceof IProjectileSpell) {
                        int cooldown = instance.getAttribute(IProjectileSpell.COOLDOWN);
                        cooldownToRemove += (int) (PROJECTILE_COOLDOWN_REDUCTION * cooldown);
                        instance.applyModifier(SPEED_MODIFIER);
                    }
                }
                event.setAdditionalCooldown(Math.max(0, event.getAdditionalCooldown() - cooldownToRemove));
            }
        }
    }
}
