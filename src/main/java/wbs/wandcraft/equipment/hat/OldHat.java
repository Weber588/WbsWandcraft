package wbs.wandcraft.equipment.hat;

import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEventUtils;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.events.CalculateManaRegenCooloffEvent;
import wbs.wandcraft.events.CalculateMaxManaEvent;
import wbs.wandcraft.events.EnqueueSpellsEvent;
import wbs.wandcraft.util.DamageUtils;

import java.util.List;

public class OldHat extends MagicHat {
    private static final double COOLOFF_REDUCTION = 0.1;
    private static final double MANA_INCREASE = 0.1;
    private static final double COOLDOWN_REDUCTION = 0.1;
    private static final double MAGIC_DAMAGE_REDUCTION = 0.1;

    public OldHat() {
        super("old", HatModel.OLD);
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
    public List<String> getEffectsLore() {
        return List.of(
                "-10% Mana Regen Cooloff",
                "+10% Max Mana",
                "-10% Spell Cooldown",
                "+10% Magic Damage Resistance"
        );
    }

    @Override
    public void registerEvents() {
        super.registerEvents();
        WbsEventUtils.register(WbsWandcraft.getInstance(), EnqueueSpellsEvent.class, this::onEnqueueSpells);
        WbsEventUtils.register(WbsWandcraft.getInstance(), CalculateMaxManaEvent.class, this::onCalculateMaxMana);
        WbsEventUtils.register(WbsWandcraft.getInstance(), CalculateManaRegenCooloffEvent.class, this::onCalculateCooloff);
        WbsEventUtils.register(WbsWandcraft.getInstance(), EntityDamageEvent.class, this::onDamage);
    }

    private void onEnqueueSpells(EnqueueSpellsEvent event) {
        ifEquipped(event.getPlayer(), () -> {
            event.setAdditionalCooldown((int) (event.getAdditionalCooldown() * (1 - COOLDOWN_REDUCTION)));
        });
    }

    private void onCalculateMaxMana(CalculateMaxManaEvent event) {
        ifEquipped(event.getPlayer(), () -> {
            event.setMaxMana((int) (event.getMaxMana() * (1 + MANA_INCREASE)));
        });
    }

    private void onCalculateCooloff(CalculateManaRegenCooloffEvent event) {
        ifEquipped(event.getPlayer(), () -> {
            event.setCooloff((int) (event.getCooloff() * (1 - COOLOFF_REDUCTION)));
        });
    }
    private void onDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity livingEntity) {
            if (DamageUtils.isMagicDamage(event.getDamageSource().getDamageType())) {
                ifEquipped(livingEntity, () -> {
                    event.setDamage(event.getDamage() * (1 - MAGIC_DAMAGE_REDUCTION));
                });
            }
        }
    }
}
