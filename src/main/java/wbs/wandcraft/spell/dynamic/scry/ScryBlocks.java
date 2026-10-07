package wbs.wandcraft.spell.dynamic.scry;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.body.ItemDialogBody;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
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
import java.util.Map;
import java.util.Set;

@NullMarked
public class ScryBlocks extends DynamicSpellScry<Material> {
    public ScryBlocks(@Nullable MagicDomain secondary) {
        super(secondary);

        setAttribute(RADIUS, 4d);
    }

    @Override
    protected ItemDialogBody.Builder getDialogBody(Material type) {
        if (type.isItem()) {
            return DialogBody.item(ItemStack.of(type));
        } else {
            return DialogBody.item(ItemStack.of(Material.BARRIER));
        }
    }

    @Override
    protected Component asComponent(Material material) {
        return Component.translatable(material.translationKey());
    }

    @Override
    public void cast(CastContext context) {
        Player player = context.player();

        SpellInstance instance = context.instance();
        double radius = instance.getAttribute(RADIUS);

        Set<Block> nearbyBlocks = getNearbyBlocks(context, radius);

        WbsWandcraft plugin = WbsWandcraft.getInstance();

        if (nearbyBlocks.isEmpty()) {
            plugin.sendActionBar("No non-air blocks within %.0f blocks!".formatted(radius), player);
            return;
        }

        Map<Material, Integer> foundBlocks = new HashMap<>();

        nearbyBlocks.forEach((block -> {
            boolean found = false;
            Material type = block.getType();
            for (Material existing : foundBlocks.keySet()) {
                if (type == existing) {
                    found = true;

                    foundBlocks.put(existing, foundBlocks.get(existing) + 1);
                    break;
                }
            }

            if (!found) {
                foundBlocks.put(type, 1);
            }
        }));

        if (foundBlocks.isEmpty()) {
            plugin.sendActionBar("No items in containers within %.0f blocks!".formatted(radius), player);
            return;
        }

        Dialog dialog = getDialog(context, foundBlocks);

        player.showDialog(dialog);
    }

    private static Set<Block> getNearbyBlocks(CastContext context, double radius) {
        Set<Block> nearbyBlocks = WbsLocationUtil.getNearbyBlocks(context.location(), radius);

        nearbyBlocks.removeIf(Block::isEmpty);
        return nearbyBlocks;
    }

    @Override
    public void onClick(CastContext context, Material clicked, Player player) {
        // TODO: Decide if clicking should do anything in this lol

//        SpellInstance instance = context.instance();
//        double radius = instance.getAttribute(RADIUS);
//
//        WbsWandcraft plugin = WbsWandcraft.getInstance();
//        Set<Block> nearbyBlocks = getNearbyBlocks(context, radius);
//
//        Set<Block> containingBlocks = new HashSet<>();
//
//        for (Block block : nearbyBlocks) {
//            // Recheck these, as the blocks or contents may have changed since the dialog was created.
//            if (block.getType() == clicked) {
//                containingBlocks.add(block);
//            }
//        }
//
//        highlightBlocks(context, player, containingBlocks);
//
//        if (!containingBlocks.isEmpty()) {
//            plugin.buildMessageNoPrefix("Found " + containingBlocks.size() + " ")
//                    .append(asComponent(clicked).applyFallbackStyle(plugin.getDefaultStyle()))
//                    .build()
//                    .sendActionBar(player);
//        } else {
//            plugin.buildMessageNoPrefix("No matches within %.0f blocks.".formatted(radius))
//                    .build()
//                    .sendActionBar(player);
//        }
    }

    @Override
    protected String asSearchableString(Material material) {
        return material.getKey().asMinimalString();
    }

    @Override
    protected @Nullable Component getHoverText(Material material) {
        return null;
    }
}
