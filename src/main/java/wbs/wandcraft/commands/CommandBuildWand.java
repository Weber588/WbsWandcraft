package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.util.ItemUtils;
import wbs.wandcraft.wand.types.WandType;

@SuppressWarnings("UnstableApiUsage")
public class CommandBuildWand extends WbsSubcommand {
    private static final WbsSimpleArgument.KeyedSimpleArgument WAND_TYPE = new WbsSimpleArgument.KeyedSimpleArgument(
            "wand_type",
            ArgumentTypes.namespacedKey(),
            WandType.WIZARDRY.getKey()
    ).addKeyedSuggestions(WandcraftRegistries.WAND_TYPES.values());
    private static final WbsSimpleArgument<Double> WAND_HUE = new WbsSimpleArgument<>(
            "wand_hue",
            DoubleArgumentType.doubleArg(-1f, 1f),
            -1d,
            Double.class
    ).addSuggestions(0d, 0.25d, 0.5d, 0.75d, 1d);

    public CommandBuildWand(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
        addSimpleArgument(WAND_TYPE);
        addSimpleArgument(WAND_HUE);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        CommandSender sender = context.getSource().getSender();

        NamespacedKey wandTypeKey = configuredArgumentMap.get(WAND_TYPE);
        if (wandTypeKey == null) {
            plugin.sendMessage("Usage: &h/" + context.getInput().split(" ")[0] + " <type> [hue]", sender);
            return 1;
        }

        WandType<?> wandType = WandcraftRegistries.WAND_TYPES.get(wandTypeKey);

        if (wandType == null) {
            plugin.sendMessage("Invalid wand type \"" + wandTypeKey.asString() + "\"!", sender);
            return 1;
        }

        Double hue = configuredArgumentMap.get(WAND_HUE);

        ItemStack wandItem = ItemUtils.buildWand(
                wandType,
                hue
        );

        if (sender instanceof Player player) {
            player.getInventory().addItem(wandItem);
        }

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        return sendSimpleArgumentUsage(context);
    }
}
