package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.effects.StatusEffectManager;
import wbs.wandcraft.spell.definitions.extensions.StatusEffectSpell;
import wbs.wandcraft.spell.definitions.type.SpellType;

public class AmorphousEarthSpell extends SpellDefinition implements StatusEffectSpell<Player> {
    public AmorphousEarthSpell() {
        super("amorphous_earth");

        addSpellType(SpellType.NATURE);
        addSpellType(SpellType.VOID);

        setAttribute(COST, 1000);
        setAttribute(COOLDOWN, 90 * Ticks.TICKS_PER_SECOND);

        setAttribute(DURATION, 30 * Ticks.TICKS_PER_SECOND);

        setAttribute(TARGET, TargeterType.SELF);
    }

    @Override
    public String rawDescription() {
        return "Allows you to walk through nearby natural blocks, seeming to phase through the world. Sneak to descend.";
    }

    @Override
    public @NotNull StatusEffect getStatusEffect() {
        return StatusEffectManager.NATURE_PHASING;
    }

    @Override
    public Class<Player> getEntityClass() {
        return Player.class;
    }
}
