package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.util.ItemUtils;
import wbs.wandcraft.wand.types.WandType;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class CommandWandBuild extends WbsSubcommand {
    private static final WbsSimpleArgument.KeyedSimpleArgument WAND_TYPE = new WbsSimpleArgument.KeyedSimpleArgument(
            "wand_type",
            ArgumentTypes.namespacedKey(),
            null
    ).addKeyedSuggestions(WandcraftRegistries.WAND_TYPES.values());

    public CommandWandBuild(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
        addSimpleArgument(WAND_TYPE);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        CommandSender sender = context.getSource().getSender();
        if (!(sender instanceof Player player)) {
            plugin.sendMessage("This command is only usable by players.", sender);
            return 1;
        }

        NamespacedKey wandTypeKey = configuredArgumentMap.get(WAND_TYPE);
        if (wandTypeKey == null) {
            List<WandType<?>> list = WandcraftRegistries.WAND_TYPES.stream().sorted(Comparator.comparing(WandType::getKey)).toList();
            if (list.size() > 54) {
                plugin.sendMessage("&wWarning! Not all wands in menu.", context.getSource().getSender());
                plugin.sendMessage("Choose a wand: "
                                + WandcraftRegistries.WAND_TYPES.stream()
                                .map(Keyed::key)
                                .map(Key::asString)
                                .collect(Collectors.joining(", ")),
                        context.getSource().getSender());
            }

            Inventory inventory = Bukkit.createInventory(null, 6 * 9, Component.text("Wands"));

            for (int i = 0; i < Math.min(inventory.getSize(), list.size()); i++) {
                inventory.setItem(i, ItemUtils.buildWand(list.get(i)));
            }

            player.openInventory(inventory);
            return Command.SINGLE_SUCCESS;
        }

        WandType<?> wandType = WandcraftRegistries.WAND_TYPES.get(wandTypeKey);

        if (wandType == null) {
            plugin.sendMessage("Invalid wand type \"" + wandTypeKey.asString() + "\"!", sender);
            return 1;
        }

        ItemStack wandItem = ItemUtils.buildWand(wandType);

        player.getInventory().addItem(wandItem);
        WbsWandcraft.getInstance().buildMessage("Got 1 ")
                .append(wandItem.effectiveName())
                .build()
                .send(sender);

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        return sendSimpleArgumentUsage(context);
    }
}
