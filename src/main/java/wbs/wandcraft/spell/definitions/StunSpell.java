package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.spell.definitions.extensions.StatusEffectSpell;

import static wbs.wandcraft.spell.definitions.type.SpellType.ARCANE;

public class StunSpell extends SpellDefinition implements StatusEffectSpell<LivingEntity> {
    public StunSpell() {
        super("stun");

        addSpellType(ARCANE);

        setAttribute(COST, 200);
        setAttribute(COOLDOWN, 15 * Ticks.TICKS_PER_SECOND);

        setAttribute(DURATION, 20);
        setAttribute(TARGET, TargeterType.RADIUS);
        setAttribute(TARGET_RANGE, 5d);
        setAttribute(MAX_TARGETS, 10);
    }

    @Override
    public String rawDescription() {
        return "Temporarily stuns all mobs in a radius, cancelling eating, drinking, and adding a short cooldown to held items.";
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
