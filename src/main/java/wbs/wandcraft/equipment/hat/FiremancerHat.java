package wbs.wandcraft.equipment.hat;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEventUtils;
import wbs.wandcraft.RegisteredPersistentDataType;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.events.EnqueueSpellsEvent;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.extensions.BurnTimeSpell;
import wbs.wandcraft.spell.definitions.extensions.DamageSpell;
import wbs.wandcraft.util.DamageUtils;

import java.util.List;

public class FiremancerHat extends MagicHat {
    private static final SpellAttributeModifier<Double, Double> DAMAGE_MODIFIER = DamageSpell.DAMAGE.createModifier(
            AttributeModifierType.MULTIPLY,
            RegisteredPersistentDataType.DOUBLE,
            1.5
    );
    private static final SpellAttributeModifier<Integer, Double> BURN_TIME_MODIFIER = BurnTimeSpell.BURN_TIME.createModifier(
            AttributeModifierType.MULTIPLY,
            RegisteredPersistentDataType.DOUBLE,
            2d
    );

    public FiremancerHat() {
        super("firemancer", HatModel.FIREMANCER);
    }

    @Override
    protected @NotNull TextColor getNameColour() {
        return TextColor.color(0x8d1b0c);
    }

    @Override
    public List<String> getEffectsLore() {
        return List.of(
                "+50% Spell Damage",
                "+100% Burn Time"
        );
    }

    @Override
    public void registerEvents() {
        super.registerEvents();
        WbsEventUtils.register(WbsWandcraft.getInstance(), EnqueueSpellsEvent.class, this::onEnqueueSpells);
        WbsEventUtils.register(WbsWandcraft.getInstance(), EntityDamageByEntityEvent.class, this::onEntityDamage);
    }

    private void onEnqueueSpells(EnqueueSpellsEvent event) {
        ifEquipped(event.getPlayer(), () -> {
            for (SpellInstance instance : event.getSpellList()) {
                instance.applyModifier(DAMAGE_MODIFIER);
                instance.applyModifier(BURN_TIME_MODIFIER);
            }
        });
    }

    private void onEntityDamage(EntityDamageByEntityEvent event) {
        DamageSource damageSource = event.getDamageSource();
        if (DamageUtils.isMagicDamage(damageSource.getDamageType())) {
            Entity damager = damageSource.getCausingEntity();
            if (damager == null) {
                damager = damageSource.getDirectEntity();
            }
            if (damager instanceof LivingEntity entity) {
                ifEquipped(entity, () -> {
                    event.setDamage(DAMAGE_MODIFIER.modify(event.getDamage()));
                });
            }
        }
    }
}
