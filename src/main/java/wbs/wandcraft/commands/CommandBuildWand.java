package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.wand.Wand;
import wbs.wandcraft.wand.WandInventoryType;

@SuppressWarnings("UnstableApiUsage")
public class CommandBuildWand extends WbsSubcommand {
    private static final WbsSimpleArgument.KeyedSimpleArgument INVENTORY_TYPE = new WbsSimpleArgument.KeyedSimpleArgument(
            "inventory_type",
            ArgumentTypes.namespacedKey(),
            WandInventoryType.PLANE_3x3.getKey()
    ).addKeyedSuggestions(WandcraftRegistries.WAND_INVENTORY_TYPES.values());

    public CommandBuildWand(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
        addSimpleArgument(INVENTORY_TYPE);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        NamespacedKey inventoryTypeKey = configuredArgumentMap.get(INVENTORY_TYPE);
        WandInventoryType inventoryType = WandcraftRegistries.WAND_INVENTORY_TYPES.get(inventoryTypeKey);

        if (inventoryType == null) {
            plugin.sendMessage("Invalid inventory type key: " + inventoryTypeKey.asString() + ".", context.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        }

        ItemStack item = ItemStack.of(Material.STICK);
        Wand wand = new Wand(inventoryType);

        item.editMeta(meta -> {
            for (ItemFlag value : ItemFlag.values()) {
                meta.addItemFlags(value);
            }
        });

        wand.toItem(item);

        if (context.getSource().getSender() instanceof Player player) {
            player.getInventory().addItem(item);
        }

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> commandContext) {
        return 0;
    }
}
