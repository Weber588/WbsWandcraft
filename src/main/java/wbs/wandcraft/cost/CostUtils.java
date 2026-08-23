package wbs.wandcraft.cost;

import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.List;

public class CostUtils {

    /**
     * Removes the set cost from the player, in the configured order.
     * @param player The player to apply the cost to
     * @param cost The cost to apply
     * @return Any remaining cost
     */
    public static int takeCost(Player player, int cost) {
        List<CostType> costTypes = CostType.getCostTypes();

        PlayerMana mana = new PlayerMana(player);
        int available = mana.getMana();

        Iterator<CostType> iterator = costTypes.iterator();
        while (available < cost && iterator.hasNext()) {
            CostType type = iterator.next();

            int manaGiven = type.apply(player, cost);

            available += manaGiven;
        }

        // Set directly, not add -- while getting mana equivalent, can exceed max mana (just can't retain it).
        mana.setMana(available);

        return mana.applyCost(player, cost);
    }
}
