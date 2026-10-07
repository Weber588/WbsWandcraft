package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.effects.StatusEffectManager;
import wbs.wandcraft.spell.definitions.extensions.StatusEffectSpell;

import static wbs.wandcraft.spell.MagicDomain.NETHER;
import static wbs.wandcraft.spell.MagicDomain.SCULK;

public class DeathWalkSpell extends SpellDefinition implements StatusEffectSpell<LivingEntity> {
    public DeathWalkSpell() {
        super("death_walk");

        setAttribute(COST, 100);
        setAttribute(COOLDOWN, 15 * Ticks.TICKS_PER_SECOND);

        addMagicDomain(SCULK);
        addMagicDomain(NETHER);

        setAttribute(DURATION, 10 * Ticks.TICKS_PER_SECOND);
        setAttribute(TARGET, TargeterType.SELF);
    }

    @Override
    protected String rawDescription() {
        return "Prevents undead from targeting you for the duration of the effect";
    }

    @Override
    public @NotNull StatusEffect getStatusEffect() {
        return StatusEffectManager.DEATH_WALK;
    }

    @Override
    public Class<LivingEntity> getEntityClass() {
        return LivingEntity.class;
    }
}
