package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.effects.StatusEffectManager;
import wbs.wandcraft.spell.RequiresPlugin;
import wbs.wandcraft.spell.definitions.extensions.StatusEffectSpell;
import wbs.wandcraft.spell.definitions.type.SpellType;

@RequiresPlugin("LibsDisguises")
@NullMarked
public class PolymorphSpell extends SpellDefinition implements StatusEffectSpell<LivingEntity> {
    public PolymorphSpell() {
        super("polymorph");

        addSpellType(SpellType.SCULK);
        addSpellType(SpellType.NATURE);

        setAttribute(COST, 500);
        setAttribute(COOLDOWN, 30 * Ticks.TICKS_PER_SECOND);

        setAttribute(DURATION, 15 * Ticks.TICKS_PER_SECOND);
        setAttribute(TARGET, TargeterType.LINE_OF_SIGHT);
        setAttribute(TARGET_RANGE, 50d);
        setAttribute(MAX_TARGETS, 1);
    }

    @Override
    public @NotNull StatusEffect getStatusEffect() {
        return StatusEffectManager.POLYMORPHED;
    }

    @Override
    public Class<LivingEntity> getEntityClass() {
        return LivingEntity.class;
    }

    @Override
    public String rawDescription() {
        return "Transforms the target entity into a sheep for a short duration";
    }
}
