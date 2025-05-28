package wbs.wandcraft.cost;

import org.bukkit.entity.Player;

public class CostUtils {

    public static void takeCost(Player player, int cost) {
        PlayerMana playerMana = new PlayerMana(player);

        int remainder = playerMana.applyCost(player, cost);

        if (remainder > 0) {
            // TODO: Pass onto next cost type
        }
    }
}
