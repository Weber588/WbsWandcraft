package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.generation.SpellInstanceGenerator;

import java.util.stream.Collectors;

public class CommandSpellGenerate extends WbsSubcommand  {
    private static final WbsSimpleArgument.KeyedSimpleArgument SPELL_GENERATOR = new WbsSimpleArgument.KeyedSimpleArgument(
            "spell_generator",
            ArgumentTypes.namespacedKey(),
            null
    ).addKeyedSuggestions(WandcraftRegistries.SPELL_GENERATORS.values());

    public CommandSpellGenerate(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
        addSimpleArgument(SPELL_GENERATOR);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        NamespacedKey generatorKey = configuredArgumentMap.get(SPELL_GENERATOR);

        if (generatorKey == null) {
            plugin.sendMessage("Choose a spell generator: "
                            + WandcraftRegistries.SPELL_GENERATORS.stream()
                            .map(Keyed::key)
                            .map(Key::asString)
                            .collect(Collectors.joining(", ")),
                    context.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        }

        SpellInstanceGenerator generator = WandcraftRegistries.SPELL_GENERATORS.get(generatorKey);

        if (generator == null) {
            plugin.sendMessage("Invalid generator key: " + generatorKey.asString() + ".", context.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        }

        if (context.getSource().getSender() instanceof Player player) {
            player.getInventory().addItem(generator.generateItem());
        }

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> commandContext) {
        return 0;
    }
}
