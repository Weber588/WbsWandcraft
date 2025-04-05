package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.spell.definitions.SpellDefinition;

import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class CommandInfo extends WbsSubcommand {
    private static final WbsSimpleArgument.KeyedSimpleArgument DEFINITION = new WbsSimpleArgument.KeyedSimpleArgument(
            "definition",
            ArgumentTypes.namespacedKey(),
            null
    ).setKeyedSuggestions(WandcraftRegistries.SPELLS.values());

    public CommandInfo(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
        
        this.addSimpleArgument(DEFINITION);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        NamespacedKey definitionKey = configuredArgumentMap.get(DEFINITION);

        if (definitionKey == null) {
            plugin.sendMessage("Choose a spell: "
                            + WandcraftRegistries.SPELLS.stream()
                            .map(Keyed::key)
                            .map(Key::asString)
                            .collect(Collectors.joining(", ")),
                    context.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        }

        SpellDefinition spell = WandcraftRegistries.SPELLS.get(definitionKey);

        if (spell == null) {
            plugin.sendMessage("Invalid spell definition: " + definitionKey.asString() + ".", context.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        }

        WbsMessageBuilder builder = plugin.buildMessage("=======================")
                .append("\nSpell: ")
                .append(spell.displayName().color(NamedTextColor.GOLD))
                .append("\nAttributes: ");

        spell.getLore().forEach(text -> {
            builder.append("\n")
                    .append(text);
        });

        builder.append("\nDescription: ")
                .append(spell.description().color(NamedTextColor.GOLD))
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
