package wbs.wandcraft.cost;

import org.bukkit.entity.Player;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.wandcraft.WbsWandcraft;

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
        for (int index = 0; index < costTypes.size(); index++) {
            CostType costType = costTypes.get(index);

            cost = costType.apply(player, cost);

            if (cost > 0) {
                WbsMessageBuilder builder = WbsWandcraft.getInstance().buildMessageNoPrefix("Out of ")
                        .append(costType.display());

                if (costTypes.size() > index + 1) {
                    CostType nextCostType = costTypes.get(index + 1);
                    builder.append(" - taking ")
                            .append(nextCostType.display());
                }

                builder.append("!");

                builder.build().sendActionBar(player);
            }

            if (cost <= 0) {
                return 0;
            }
        }

        return cost;
    }
}
