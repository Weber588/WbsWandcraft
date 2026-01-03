package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.spell.definitions.SpellDefinition;

import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public abstract class SpellSubcommand extends WbsSubcommand {
    private static final WbsSimpleArgument.KeyedSimpleArgument DEFINITION = new WbsSimpleArgument.KeyedSimpleArgument(
            "definition",
            ArgumentTypes.namespacedKey(),
            null
    ).setKeyedSuggestions(WandcraftRegistries.SPELLS.values());

    public SpellSubcommand(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
        
        this.addSimpleArgument(DEFINITION);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        NamespacedKey definitionKey = configuredArgumentMap.get(DEFINITION);

        CommandSender sender = context.getSource().getSender();

        if (definitionKey == null) {
            plugin.sendMessage("Choose a spell: "
                            + WandcraftRegistries.SPELLS.stream()
                            .map(Keyed::key)
                            .map(Key::asString)
                            .collect(Collectors.joining(", ")),
                    sender);
            return Command.SINGLE_SUCCESS;
        }

        if (definitionKey.value().equals("all")) {
            return runSpellCommandAll(context, sender);
        }

        SpellDefinition spell = WandcraftRegistries.SPELLS.get(definitionKey);

        if (spell == null) {
            plugin.sendMessage("Invalid spell definition: " + definitionKey.asString() + ".", sender);
            return Command.SINGLE_SUCCESS;
        }

        return runSpellCommand(context, sender, spell);
    }

    protected abstract int runSpellCommand(CommandContext<CommandSourceStack> context, CommandSender sender, SpellDefinition spell);
    protected abstract int runSpellCommandAll(CommandContext<CommandSourceStack> context, CommandSender sender);

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        plugin.sendMessage("Usage: &h/" + context.getInput() + " <spell>", context.getSource().getSender());
        return Command.SINGLE_SUCCESS;
    }
}
