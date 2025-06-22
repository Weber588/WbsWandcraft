package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.spell.definitions.extensions.StatusEffectSpell;

public class CharmSpell extends SpellDefinition implements StatusEffectSpell<Mob> {
    public CharmSpell() {
        super("charm");

        setAttribute(DURATION, 15 * Ticks.TICKS_PER_SECOND);
        setAttribute(TARGET, TargeterType.RADIUS);
        setAttribute(TARGET_RANGE, 5d);
        setAttribute(MAX_TARGETS, 3);
    }

    @Override
    public Component description() {
        return Component.text("Prevents undead from targeting you for the duration of the effect");
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
