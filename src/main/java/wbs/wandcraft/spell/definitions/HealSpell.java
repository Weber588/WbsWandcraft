package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.TargetedHealthSpell;
import wbs.wandcraft.spell.definitions.type.SpellType;

@NullMarked
public class HealSpell extends SpellDefinition implements CastableSpell, TargetedHealthSpell {
    public HealSpell() {
        super("heal");

        addSpellType(SpellType.NATURE);

        setAttribute(COST, 300);
        setAttribute(COOLDOWN, 10 * Ticks.TICKS_PER_SECOND);

        setAttribute(HEALTH, 2d);

        setAttribute(TARGET, TargeterType.SELF);
        setAttribute(TARGET_RANGE, 10d);
    }

    @Override
    public String rawDescription() {
        return "Instantly heals the target.";
    }

    @Override
    public void cast(CastContext context) {
        healTargetsWithParticles(context);
    }

    @Override
    public Class<LivingEntity> getEntityClass() {
        return LivingEntity.class;
    }
}
