package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.TargetedSpell;
import wbs.wandcraft.spell.definitions.type.SpellType;

import java.util.List;

public class InterruptSpell extends SpellDefinition implements CastableSpell, TargetedSpell<LivingEntity> {
    public InterruptSpell() {
        super("interrupt");

        addSpellType(SpellType.SCULK);
        addSpellType(SpellType.VOID);

        setAttribute(COST, 100);
        setAttribute(COOLDOWN, 10 * Ticks.TICKS_PER_SECOND);

        setAttribute(TARGET, TargeterType.LINE_OF_SIGHT);
        setAttribute(TARGET_RANGE, 50d);
    }

    @Override
    public String rawDescription() {
        return "Cancels item usage, such as eating, using a spellbook, drawing a bow, or casting certain wands.";
    }

    @Override
    public void cast(CastContext context) {
        List<LivingEntity> targets = getTargets(context);

        for (LivingEntity target : targets) {
            target.getWorld().playSound(target, Sound.BLOCK_BEACON_DEACTIVATE, 0.75f, 2);
            target.clearActiveItem();
        }
    }

    @Override
    public Class<LivingEntity> getEntityClass() {
        return LivingEntity.class;
    }
}
