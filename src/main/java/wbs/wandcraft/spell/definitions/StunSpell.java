package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.spell.definitions.extensions.StatusEffectSpell;

public class StunSpell extends SpellDefinition implements StatusEffectSpell<LivingEntity> {
    public StunSpell() {
        super("stun");

        setAttribute(DURATION, 20);
        setAttribute(TARGET, TargeterType.RADIUS);
        setAttribute(TARGET_RANGE, 5d);
        setAttribute(MAX_TARGETS, 10);
    }

    @Override
    public Component description() {
        return Component.text("Temporarily stuns all mobs in a radius, cancelling eating, drinking, and adding a short cooldown to held items.");
    }

    @Override
    public @NotNull StatusEffect getStatusEffect() {
        return StatusEffect.STUNNED;
    }

    @Override
    public Class<LivingEntity> getEntityClass() {
        return LivingEntity.class;
    }
}
