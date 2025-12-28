package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spellbook.Spellbook;

import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class CommandSpellLearn extends WbsSubcommand {
    private static final WbsSimpleArgument.KeyedSimpleArgument DEFINITION = new WbsSimpleArgument.KeyedSimpleArgument(
            "definition",
            ArgumentTypes.namespacedKey(),
            null
    ).setKeyedSuggestions(WandcraftRegistries.SPELLS.values());

    public CommandSpellLearn(@NotNull WbsPlugin plugin, @NotNull String label) {
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

        SpellDefinition spell = WandcraftRegistries.SPELLS.get(definitionKey);

        if (spell == null) {
            plugin.sendMessage("Invalid spell definition: " + definitionKey.asString() + ".", sender);
            return Command.SINGLE_SUCCESS;
        }

        if (!(sender instanceof Player player)) {
            plugin.sendMessage("This command is only usable by players", sender);
            return 1;
        }

        Spellbook.teachSpell(player, spell);

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        plugin.sendMessage("Usage: &h/" + context.getInput() + " <spell>", context.getSource().getSender());
        return Command.SINGLE_SUCCESS;
    }
}
