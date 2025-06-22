package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.spell.definitions.extensions.StatusEffectSpell;

public class PlanarBindingSpell extends SpellDefinition implements StatusEffectSpell<LivingEntity> {
    public PlanarBindingSpell() {
        super("planar_binding");

        setAttribute(DURATION, 100);
        setAttribute(TARGET, TargeterType.LINE_OF_SIGHT);
        setAttribute(TARGET_RANGE, 50d);
        setAttribute(MAX_TARGETS, 1);
    }

    @Override
    public Component description() {
        return Component.text("Prevents all nearby entities from teleporting for a short duration.");
    }

    @Override
    public @NotNull StatusEffect getStatusEffect() {
        return StatusEffect.PLANAR_BINDING;
    }

    @Override
    public Class<LivingEntity> getEntityClass() {
        return LivingEntity.class;
    }
}
