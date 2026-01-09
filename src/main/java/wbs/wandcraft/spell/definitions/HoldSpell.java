package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.effects.StatusEffectManager;
import wbs.wandcraft.spell.definitions.extensions.StatusEffectSpell;
import wbs.wandcraft.spell.definitions.type.SpellType;

public class HoldSpell extends SpellDefinition implements StatusEffectSpell<LivingEntity> {
    public HoldSpell() {
        super("hold");

        addSpellType(SpellType.VOID);
        addSpellType(SpellType.SCULK);

        setAttribute(DURATION, 10 * Ticks.TICKS_PER_SECOND);
    }

    @Override
    public String rawDescription() {
        return "The target is held in place, unable to move for a short duration.";
    }

    @Override
    public @NotNull StatusEffect getStatusEffect() {
        return StatusEffectManager.HOLD;
    }

    @Override
    public Class<LivingEntity> getEntityClass() {
        return LivingEntity.class;
    }
}
