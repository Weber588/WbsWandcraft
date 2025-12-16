package wbs.wandcraft.generation;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.configuration.ConfigurableCondition;

import java.util.LinkedList;
import java.util.List;

public class ConfiguredTrade implements Keyed {
    private final NamespacedKey key;
    private final TradeType type;
    private final WandGenerator generator;
    private final List<ConfigurableCondition> conditions = new LinkedList<>();

    public ConfiguredTrade(NamespacedKey key, TradeType type, WandGenerator generator) {
        this.key = key;
        this.type = type;
        this.generator = generator;
    }

    public MerchantRecipe buildNewRecipe() {
        return new MerchantRecipe(generator.get(),
                        0,
                        1,
                        false,
                        0,
                        1,
                        true);
    }

    public TradeType getType() {
        return type;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public enum TradeType {
        WANDERING_TRADER,
        PIGLIN_BARTERING,
        VILLAGER
    }
}
