package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.spell.definitions.extensions.StatusEffectSpell;

import static wbs.wandcraft.spell.definitions.type.SpellType.NATURE;
import static wbs.wandcraft.spell.definitions.type.SpellType.SCULK;

public class CharmSpell extends SpellDefinition implements StatusEffectSpell<Mob> {
    public CharmSpell() {
        super("charm");

        addSpellType(NATURE);
        addSpellType(SCULK);

        setAttribute(COST, 300);
        setAttribute(COOLDOWN, 15 * Ticks.TICKS_PER_SECOND);

        setAttribute(DURATION, 15 * Ticks.TICKS_PER_SECOND);
        setAttribute(TARGET, TargeterType.RADIUS);
        setAttribute(TARGET_RANGE, 5d);
        setAttribute(MAX_TARGETS, 3);
    }

    @Override
    public String rawDescription() {
        return "Prevents undead from targeting you for the duration of the effect";
    }

    @Override
    public @NotNull StatusEffect getStatusEffect() {
        return StatusEffect.CHARMED;
    }

    @Override
    public Class<Mob> getEntityClass() {
        return Mob.class;
    }
}
