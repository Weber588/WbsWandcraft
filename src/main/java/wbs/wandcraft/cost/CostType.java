package wbs.wandcraft.cost;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.Ticks;
import org.bukkit.GameMode;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsMath;
import wbs.wandcraft.WbsWandcraft;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

@NullMarked
public class CostType implements Keyed {
    private static final List<CostType> COST_TYPE_ORDER = new LinkedList<>();

    public static List<CostType> getCostTypes() {
        return Collections.unmodifiableList(COST_TYPE_ORDER);
    }

    // TODO: Make these configurable for order, mana equivalent, and whether or not it's active
    public static final CostType MANA = new CostType(
            "mana",
            Component.text("Mana").color(NamedTextColor.AQUA),
            (player, cost) -> {
                PlayerMana playerMana = new PlayerMana(player);

                return playerMana.applyCost(player, cost);
            }
    );
    public static final CostType EXPERIENCE = new CostType(
            "experience",
            Component.text("Levels").color(TextColor.color(0x7efc20)),
            (player, cost) -> {
                if (player.getGameMode() == GameMode.CREATIVE) {
                    return 0;
                }

                int level = player.getLevel();

                if (level >= cost) {
                    player.setLevel(level - cost);
                    return 0;
                } else {
                    player.setLevel(0);
                    return cost - level;
                }
            }
    ).manaEquivalent(50);
    public static final CostType HUNGER = new CostType(
            "hunger",
            Component.text("Hunger").color(TextColor.color(0xffab53)),
            (player, cost) -> {
                if (player.getGameMode() == GameMode.CREATIVE) {
                    return 0;
                }

                float floatCost = (float) cost;
                float saturation = player.getSaturation();

                if (saturation >= Math.ceil(floatCost)) {
                    player.setSaturation(saturation - floatCost);
                    return 0;
                } else {
                    player.setSaturation(0);
                    floatCost -= saturation;
                }

                int foodLevel = player.getFoodLevel();

                if (foodLevel >= Math.ceil(floatCost)) {
                    player.setFoodLevel(foodLevel - (int) Math.ceil(floatCost));
                    return 0;
                } else {
                    player.setFoodLevel(0);
                    return (int) Math.ceil(floatCost) - foodLevel;
                }
            }
    ).manaEquivalent(50);

    // TODO: Make this configurable
    private static final Set<PotionEffect> FATIGUE_EFFECTS = Set.of(
            new PotionEffect(PotionEffectType.BLINDNESS, 10 * Ticks.TICKS_PER_SECOND, 0, true, true, true),
            new PotionEffect(PotionEffectType.NAUSEA, 10 * Ticks.TICKS_PER_SECOND, 0, true, true, true),
            new PotionEffect(PotionEffectType.SLOWNESS, 10 * Ticks.TICKS_PER_SECOND, 0, true, true, true),
            new PotionEffect(PotionEffectType.WEAKNESS, 10 * Ticks.TICKS_PER_SECOND, 0, true, true, true)
    );

    public static final CostType FATIGUE = new CostType(
            "fatigue",
            Component.text("Fatigue").color(NamedTextColor.RED),
            (player, cost) -> {
                // Randomly give the player fatigue effects and forgive the entire cost. Chance gets worse to forgive
                // when they have more of the effects, so it becomes more likely to go to the next level.
                for (PotionEffect fatigueEffect : FATIGUE_EFFECTS) {
                    if (!player.hasPotionEffect(fatigueEffect.getType())) {
                        if (WbsMath.chance(2 * 100.0 / FATIGUE_EFFECTS.size())) {
                            player.addPotionEffect(fatigueEffect);
                            return 0;
                        }
                    }
                }

                return cost;
            }
    ).manaEquivalent(50);
    public static final CostType HEALTH = new CostType(
            "health",
            Component.text("Health").color(NamedTextColor.DARK_RED),
            (player, cost) -> {
                if (player.getGameMode() == GameMode.CREATIVE) {
                    return 0;
                }

                double health = player.getHealth();
                if (health >= cost + 1) {
                    player.damage(cost, DamageSource.builder(DamageType.MAGIC).withDirectEntity(player).build());
                    return 0;
                }

                return cost;
            }
    ).manaEquivalent(50);

    private final NamespacedKey key;
    private final Component display;
    private final BiFunction<Player, Integer, Integer> applyFunction;
    private int manaEquivalent = 1;

    private CostType(String key, Component display, BiFunction<Player, Integer, Integer> applyFunction) {
        this(applyFunction, WbsWandcraft.getKey(key), display);
    }
    public CostType(BiFunction<Player, Integer, Integer> applyFunction, NamespacedKey key, Component display) {
        this.applyFunction = applyFunction;
        this.key = key;
        this.display = display;
        COST_TYPE_ORDER.add(this);
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    public Component display() {
        return display;
    }

    public int manaEquivalent() {
        return manaEquivalent;
    }

    public CostType manaEquivalent(int manaEquivalent) {
        this.manaEquivalent = manaEquivalent;
        return this;
    }

    public int apply(Player player, int cost) {
        int modifiedCost = (int) Math.ceil((double) cost / manaEquivalent);

        return applyFunction.apply(player, modifiedCost) * manaEquivalent;
    }
}
