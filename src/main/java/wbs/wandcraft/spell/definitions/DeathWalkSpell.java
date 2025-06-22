package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.spell.definitions.extensions.StatusEffectSpell;

public class DeathWalkSpell extends SpellDefinition implements StatusEffectSpell<LivingEntity> {
    public DeathWalkSpell() {
        super("death_walk");

        setAttribute(DURATION, 200);
        setAttribute(TARGET, TargeterType.SELF);
    }

    @Override
    public Component description() {
        return Component.text("Prevents undead from targeting you for the duration of the effect");
    }

    @Override
    public @NotNull StatusEffect getStatusEffect() {
        return StatusEffect.DEATH_WALK;
    }

    @Override
    public Class<LivingEntity> getEntityClass() {
        return LivingEntity.class;
    }
}
