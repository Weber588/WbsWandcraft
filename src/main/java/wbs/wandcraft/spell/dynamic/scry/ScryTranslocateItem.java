package wbs.wandcraft.spell.dynamic.scry;

import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.inventory.WbsInventoryUtil;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.MagicDomain;
import wbs.wandcraft.spell.definitions.SpellInstance;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@NullMarked
public class ScryTranslocateItem extends ScryNearbyContainers {
    public ScryTranslocateItem(@Nullable MagicDomain secondary) {
        super(secondary);

        setAttribute(RADIUS, 5d);
    }

    @Override
    public void onClick(CastContext context, ItemStack clicked, Player player) {
        SpellInstance instance = context.instance();
        double radius = instance.getAttribute(RADIUS);

        WbsWandcraft plugin = WbsWandcraft.getInstance();
        Map<Block, Container> allContainers = getContainersWith(context, radius, clicked);

        Set<Block> takenFrom = new HashSet<>();

        int amount = 0;

        for (Map.Entry<Block, Container> entry : allContainers.entrySet()) {
            Block block = entry.getKey();
            Container container = entry.getValue();

            Inventory inventory = getInventory(container);
            Map<Integer, ItemStack> transferred = WbsInventoryUtil.transferItems(
                    inventory,
                    player.getInventory(),
                    clicked::isSimilar
            );

            for (ItemStack success : transferred.values()) {
                amount += success.getAmount();
            }

            takenFrom.add(block);
        }

        highlightBlocks(context, player, takenFrom);

        if (amount > 0) {
            plugin.buildMessageNoPrefix("Translocated x%d ".formatted(amount))
                    .append(clicked.effectiveName().applyFallbackStyle(plugin.getDefaultStyle()))
                    .append(" from %d containers!".formatted(takenFrom.size()))
                    .build()
                    .sendActionBar(player);
        } else {
            plugin.buildMessageNoPrefix("Not enough inventory space!")
                    .build()
                    .sendActionBar(player);
        }
    }

    @Override
    protected @Nullable Component getHoverText(ItemStack itemStack) {
        return Component.text("Click to translocate items");
    }

    @Override
    public Component displayName() {
        return Component.text("Translocate Item").color(getTypeColours().getLast());
    }
}
