package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.spell.definitions.extensions.StatusEffectSpell;

import static wbs.wandcraft.spell.definitions.type.SpellType.NETHER;
import static wbs.wandcraft.spell.definitions.type.SpellType.SCULK;

public class DeathWalkSpell extends SpellDefinition implements StatusEffectSpell<LivingEntity> {
    public DeathWalkSpell() {
        super("death_walk");

        setAttribute(COST, 100);
        setAttribute(COOLDOWN, 15 * Ticks.TICKS_PER_SECOND);

        addSpellType(SCULK);
        addSpellType(NETHER);

        setAttribute(DURATION, 10 * Ticks.TICKS_PER_SECOND);
        setAttribute(TARGET, TargeterType.SELF);
    }

    @Override
    public String rawDescription() {
        return "Prevents undead from targeting you for the duration of the effect";
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
