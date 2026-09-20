package wbs.wandcraft.spell.attributes.attributable;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import wbs.utils.util.pluginhooks.WbsRegionUtils;
import wbs.wandcraft.AttributeDataType;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.SpellType;
import wbs.wandcraft.spell.attributes.AttributeHolder;
import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.KeyedSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.definitions.ISpellDefinition;
import wbs.wandcraft.util.DistanceScaler;

import java.util.function.Consumer;

public interface DamageAttributable extends AttributeHolder, DistanceScaler {
    SpellAttribute<Double> DAMAGE = new DoubleSpellAttribute("damage", 1.0)
            .addSuggestions(1.0, 2.0, 5.0)
            .setShowAttribute(value -> value > 0)
            .typeModifiers(SpellType.NETHER, 2d, null, 1d)
            .typeModifiers(SpellType.VOID, 3d, null, 2d);
    SpellAttribute<DamageType> DAMAGE_TYPE = new KeyedSpellAttribute<>(
            "damage_type",
            DamageType.MAGIC,
            AttributeDataType.DAMAGE_TYPE,
            "damage type",
            k -> RegistryAccess.registryAccess().getRegistry(RegistryKey.DAMAGE_TYPE).get(k)
    ).addSuggestions(RegistryAccess.registryAccess().getRegistry(RegistryKey.DAMAGE_TYPE).stream().toList())
            .typeModifier(SpellType.NETHER, AttributeModifierType.SET, DamageType.IN_FIRE);

    @AttributableSetupHandler
    default void setUpDamage() {
        addAttribute(DAMAGE);
        addAttribute(DAMAGE_TYPE);
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

        String usingNoun = getUsingNoun();
        return Component.text(messageFormat.formatted(killer.getName(), victim.getName(), usingNoun));
    }

    default @NonNull String getUsingNoun() {
        String usingNoun = "magic";
        if (this instanceof ISpellDefinition spellDefinition) {
            usingNoun = spellDefinition.name();
        }
        return usingNoun;
    }

    default DamageSource.Builder buildDamageSource(CastContext context) {
        return buildDamageSource(context, getDamageType(context));
    }
    default DamageSource.Builder buildDamageSource(CastContext context, DamageType type) {
        DamageSource.Builder builder = DamageSource.builder(type);

        Player player = context.getOnlinePlayer();
        if (player != null) {
            builder = builder.withDirectEntity(player);
            builder = builder.withCausingEntity(player);
        }

        return builder;
    }

    default double damage(CastContext context, Damageable target) {
        return damage(context, target, getDamageType(context));
    }
    default double damageScaledByDistance(CastContext context, Damageable target, double varianceClamp) {
        return damage(context, target, scaleByDistance(context, target, varianceClamp, DAMAGE), getDamageType(context));
    }

    default double damage(CastContext context, Damageable target, double damage) {
        return damage(context, target, damage, getDamageType(context));
    }
    default double damage(CastContext context, Damageable target, DamageType type) {
        double damage = context.instance().getAttribute(DAMAGE);
        return damage(context, target, damage, type);
    }
    default double damage(CastContext context, Damageable target, double damage, DamageType type) {
        return damage(context, target, damage, type, null);
    }
    default double damage(CastContext context, Damageable target, double damage, DamageType type, @Nullable Consumer<DamageSource.Builder> modifySource) {
        if (damage <= 0) {
            return 0;
        }

        if (WbsRegionUtils.canDealDamage(context.player(), target)) {
            double health = target.getHealth();
            DamageSource.Builder sourceBuilder = buildDamageSource(context, type);
            if (modifySource != null) {
                modifySource.accept(sourceBuilder);
            }
            target.damage(damage, sourceBuilder.build());
            if (health > target.getHealth()) {
                return health - target.getHealth();
            }
        }

        return 0;
    }

    default double damageThen(Entity entity, CastContext context, Consumer<Damageable> then) {
        return damageThen(entity, context, context.instance().getAttribute(DAMAGE), then);
    }

    default double damageThen(Entity entity, CastContext context, double damage, Consumer<Damageable> then) {
        if (entity instanceof Damageable damageable) {
            double damageDealt = damage(context, damageable, damage, getDamageType(context));
            if (damageDealt > 0) {
                then.accept(damageable);
            }
            return damageDealt;
        }
        return 0;
    }

    default DamageType getDamageType(CastContext context) {
        return context.instance().getAttribute(DAMAGE_TYPE);
    }
}
