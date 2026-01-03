package wbs.wandcraft.spell.definitions.extensions;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

public interface DamageSpell extends ISpellDefinition {
    SpellAttribute<Double> DAMAGE = new DoubleSpellAttribute("damage", 1.0)
            .addSuggestions(1.0, 2.0, 5.0)
            .setShowAttribute(value -> value > 0);

    default void setUpDamage() {
        addAttribute(DAMAGE);
    }

    default @Nullable String getDeathMessageFormat() {
        String killedVerb = getKilledVerb();
        return "%1$s was " + killedVerb + " by %2$s using %3$s!";
    }

    default @NotNull String getKilledVerb() {
        return "killed";
    }

    default @Nullable String getSuicideMessageFormat() {
        String killedVerb = getKilledVerb();
        return "%1$s " + killedVerb + " themself using %3$s!";
    }

    default @Nullable Component getDeathMessage(Player killer, Player victim) {
        String messageFormat = getDeathMessageFormat();

        if (killer.equals(victim)) {
            String suicideMessageFormat = getSuicideMessageFormat();
            if (suicideMessageFormat != null) {
                messageFormat = suicideMessageFormat;
            }
        }

        if (messageFormat == null) {
            return null;
        }

        return Component.text(messageFormat.formatted(killer.getName(), victim.getName(), name()));
    }
}
