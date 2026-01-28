package wbs.wandcraft.spell.definitions.extensions;

import net.kyori.adventure.text.Component;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.context.CastContext;
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

    default DamageSource.Builder buildDamageSource(CastContext context) {
        return buildDamageSource(context, DamageType.MAGIC);
    }
    default DamageSource.Builder buildDamageSource(CastContext context, DamageType type) {
        DamageSource.Builder builder = DamageSource.builder(type);

        Player player = context.getOnlinePlayer();
        if (player != null) {
            builder = builder.withDirectEntity(player);
        }

        return builder;
    }

    default void damage(CastContext context, Damageable target) {
        damage(context, target, DamageType.MAGIC);
    }
    default void damage(CastContext context, Damageable target, double damage) {
        damage(context, target, damage, DamageType.MAGIC);
    }
    default void damage(CastContext context, Damageable target, DamageType type) {
        double damage = context.instance().getAttribute(DAMAGE);
        damage(context, target, damage, type);
    }
    default void damage(CastContext context, Damageable target, double damage, DamageType type) {
        target.damage(damage, buildDamageSource(context, type).build());
    }
}
