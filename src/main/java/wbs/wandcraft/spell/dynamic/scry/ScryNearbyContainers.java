package wbs.wandcraft.spell.dynamic.scry;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.body.ItemDialogBody;
import net.kyori.adventure.text.Component;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.WbsLocationUtil;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.MagicDomain;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.dynamic.DynamicSpellScry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@NullMarked
public class ScryNearbyContainers extends DynamicSpellScry<ItemStack> {
    public ScryNearbyContainers(@Nullable MagicDomain secondary) {
        super(secondary);

        setAttribute(RADIUS, 8d);
    }

    @Override
    protected ItemDialogBody.Builder getDialogBody(ItemStack itemStack) {
        return DialogBody.item(itemStack);
    }

    @Override
    protected Component asComponent(ItemStack itemStack) {
        return itemStack.effectiveName();
    }

    @Override
    public void cast(CastContext context) {
        Player player = context.player();

        SpellInstance instance = context.instance();
        double radius = instance.getAttribute(RADIUS);

        Map<Block, Container> nearbyContainers = getContainersWith(context, radius, null);

        WbsWandcraft plugin = WbsWandcraft.getInstance();

        if (nearbyContainers.isEmpty()) {
            plugin.sendActionBar("No containers within %.0f blocks!".formatted(radius), player);
            return;
        }

        Map<ItemStack, Integer> containedItems = new HashMap<>();

        nearbyContainers.forEach(((block, container) -> {
            for (ItemStack item : getInventory(container)) {
                if (item != null) {
                    boolean found = false;
                    for (ItemStack existing : containedItems.keySet()) {
                        if (item.isSimilar(existing)) {
                            found = true;

                            containedItems.put(existing, containedItems.get(existing) + item.getAmount());
                            break;
                        }
                    }

                    if (!found) {
                        containedItems.put(item.asOne(), item.getAmount());
                    }
                }
            }
        }));

        if (containedItems.isEmpty()) {
            plugin.sendActionBar("No items in containers within %.0f blocks!".formatted(radius), player);
            return;
        }

        Dialog dialog = getDialog(context, containedItems);

        player.showDialog(dialog);
    }

    @Override
    public void onClick(CastContext context, ItemStack clicked, Player player) {
        SpellInstance instance = context.instance();
        double radius = instance.getAttribute(RADIUS);

        WbsWandcraft plugin = WbsWandcraft.getInstance();
        Map<Block, Container> allContainers = getContainersWith(context, radius, clicked);

        Set<Block> containingBlocks = new HashSet<>();

        for (Block block : allContainers.keySet()) {
            // Recheck these, as the blocks or contents may have changed since the dialog was created.
            if (block.getState() instanceof Container container) {
                Inventory containerInventory = getInventory(container);
                if (containerInventory.containsAtLeast(clicked, 1)) {
                    containingBlocks.add(block);
                }
            }
        }

        highlightBlocks(context, player, containingBlocks);

        if (!containingBlocks.isEmpty()) {
            plugin.buildMessageNoPrefix(containingBlocks.size() + " inventories contain ")
                    .append(clicked.effectiveName().applyFallbackStyle(plugin.getDefaultStyle()))
                    .build()
                    .sendActionBar(player);
        } else {
            plugin.buildMessageNoPrefix("No matches within %.0f blocks.".formatted(radius))
                    .build()
                    .sendActionBar(player);
        }
    }

    @Override
    protected String asSearchableString(ItemStack item) {
        return item.getType().getKey().asMinimalString() + item.getItemMeta().getAsComponentString();
    }

    @Override
    protected @Nullable Component getHoverText(ItemStack itemStack) {
        return Component.text("Click to search!");
    }

    protected Inventory getInventory(Container container) {
        Inventory containerInventory;
        if (container instanceof Chest chest) {
            containerInventory = chest.getBlockInventory();
        } else {
            containerInventory = container.getInventory();
        }
        return containerInventory;
    }

    protected Map<Block, Container> getContainersWith(CastContext context, double radius, @Nullable ItemStack searchItem) {
        Map<Block, Container> allContainers = new HashMap<>();

        for (Block block : WbsLocationUtil.getNearbyBlocksSphere(context.location(), radius)) {
            if (block.getState() instanceof Container container) {
                Inventory inventory = getInventory(container);
                if (searchItem == null || inventory.containsAtLeast(searchItem, 1)) {
                    allContainers.put(block, container);
                }
            }
        }
        return allContainers;
    }

    @Override
    public Component displayName() {
        return Component.text("Discover Item").color(getTypeColours().getLast());
    }
}
