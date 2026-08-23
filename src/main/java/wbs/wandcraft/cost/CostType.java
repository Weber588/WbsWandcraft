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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.WbsWandcraft;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
@NullMarked
public class CostType implements Keyed {
    private static final List<CostType> COST_TYPE_ORDER = new LinkedList<>();

    public static List<CostType> getCostTypes() {
        return Collections.unmodifiableList(COST_TYPE_ORDER);
    }

    public static final CostType EXPERIENCE = new CostType(
            "experience",
            Component.text("Levels").color(TextColor.color(0x7efc20)),
            (player, cost) -> {
                if (player.getGameMode() == GameMode.CREATIVE) {
                    return cost;
                }

                int level = player.getLevel();

                if (level >= cost) {
                    player.setLevel(level - cost);
                    return cost;
                } else {
                    player.setLevel(0);
                    return level;
                }
            }
    ).manaEquivalent(50);

    public static final CostType SATURATION = new CostType(
            "saturation",
            Component.text("Saturation").color(TextColor.color(0xffab53)),
            (player, cost) -> {
                if (player.getGameMode() == GameMode.CREATIVE) {
                    return cost;
                }

                float saturation = (float) Math.ceil(player.getSaturation());

                if (saturation >= cost) {
                    player.setSaturation(saturation - cost);
                    return cost;
                } else {
                    player.setSaturation(0);
                    return (int) saturation;
                }
            }
    ).manaEquivalent(50);

    public static final CostType HUNGER = new CostType(
            "hunger",
            Component.text("Hunger").color(TextColor.color(0xffab53)),
            (player, cost) -> {
                if (player.getGameMode() == GameMode.CREATIVE) {
                    return cost;
                }

                int foodLevel = player.getFoodLevel();

                if (foodLevel >= cost) {
                    player.setFoodLevel(foodLevel - cost);
                    return cost;
                } else {
                    player.setFoodLevel(0);
                    return foodLevel;
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
                int applied = 0;

                // Randomly give the player fatigue effects and forgive the entire cost. Chance gets worse to forgive
                // when they have more of the effects, so it becomes more likely to go to the next level.
                for (PotionEffect fatigueEffect : FATIGUE_EFFECTS) {
                    if (!player.hasPotionEffect(fatigueEffect.getType())) {
                        player.addPotionEffect(fatigueEffect);
                        applied += 1;

                        if (cost <= applied) {
                            return cost;
                        }
                    }
                }

                return applied;
            }
    ).manaEquivalent(100);
    public static final NamespacedKey DAMAGED_BY_COST = WbsWandcraft.getKey("damaged_by_cost");
    public static final CostType HEALTH = new CostType(
            "health",
            Component.text("Health").color(NamedTextColor.DARK_RED),
            (player, cost) -> {
                if (player.getGameMode() == GameMode.CREATIVE) {
                    return cost;
                }

                double health = player.getHealth();
                player.getPersistentDataContainer().set(DAMAGED_BY_COST, PersistentDataType.BOOLEAN, true);
                player.damage(cost, DamageSource.builder(DamageType.MAGIC).build());
                player.getPersistentDataContainer().remove(DAMAGED_BY_COST);

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

    /**
     * @return How much mana equivalent was provided
     */
    public int apply(Player player, int cost) {
        int modifiedCost = (int) Math.ceil((double) cost / manaEquivalent);

        return applyFunction.apply(player, modifiedCost) * manaEquivalent;
    }
}
