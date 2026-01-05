package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.wand.types.WandType;

import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class CommandWandInfo extends WbsSubcommand {
    private static final WbsSimpleArgument.KeyedSimpleArgument WAND_TYPE = new WbsSimpleArgument.KeyedSimpleArgument(
            "wand_type",
            ArgumentTypes.namespacedKey(),
            null
    ).addKeyedSuggestions(WandcraftRegistries.WAND_TYPES.values());

    public CommandWandInfo(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
        
        this.addSimpleArgument(WAND_TYPE);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        NamespacedKey definitionKey = configuredArgumentMap.get(WAND_TYPE);

        if (definitionKey == null) {
            plugin.sendMessage("Choose a wand type: "
                            + WandcraftRegistries.WAND_TYPES.stream()
                            .map(Keyed::key)
                            .map(Key::asString)
                            .collect(Collectors.joining(", ")),
                    context.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        }

        WandType<?> type = WandcraftRegistries.WAND_TYPES.get(definitionKey);

        if (type == null) {
            plugin.sendMessage("Invalid wand definition: " + definitionKey.asString() + ".", context.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        }

        WbsMessageBuilder builder = plugin.buildMessageNoPrefix("=======================")
                .append("\nWand: ")
                .append(type.getItemName().color(NamedTextColor.GOLD))
                .append("\nDescription: ")
                .append(Component.text(type.getRawDescription()).color(NamedTextColor.GOLD));

        builder
                .append("\n=======================")
                .send(context.getSource().getSender());

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        plugin.sendMessage("Usage: &h/" + context.getInput() + " <spell>", context.getSource().getSender());
        return Command.SINGLE_SUCCESS;
    }
}
