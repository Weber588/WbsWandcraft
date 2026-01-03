package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.spell.definitions.extensions.StatusEffectSpell;

import static wbs.wandcraft.spell.definitions.type.SpellType.ENDER;
import static wbs.wandcraft.spell.definitions.type.SpellType.SCULK;

public class PlanarBindingSpell extends SpellDefinition implements StatusEffectSpell<LivingEntity> {
    public PlanarBindingSpell() {
        super("planar_binding");

        addSpellType(SCULK);
        addSpellType(ENDER);

        setAttribute(COST, 100);
        setAttribute(COOLDOWN, 5 * Ticks.TICKS_PER_SECOND);

        setAttribute(DURATION, 10 * Ticks.TICKS_PER_SECOND);
        setAttribute(TARGET, TargeterType.LINE_OF_SIGHT);
        setAttribute(TARGET_RANGE, 50d);
        setAttribute(MAX_TARGETS, 1);
    }

    @Override
    public String rawDescription() {
        return "Prevents all nearby entities from teleporting for a short duration.";
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
