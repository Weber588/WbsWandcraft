package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument.KeyedSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastingQueue;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;

@SuppressWarnings("UnstableApiUsage")
public class CommandSpellCast extends WbsSubcommand {
    private static final KeyedSimpleArgument DEFINITION = new KeyedSimpleArgument(
            "definition",
            ArgumentTypes.namespacedKey(),
            null
    ).setKeyedSuggestions(WandcraftRegistries.SPELLS.values());

    public CommandSpellCast(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
        addSimpleArgument(DEFINITION);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        if (!(context.getSource().getSender() instanceof Player player)) {
            plugin.sendMessage("This command is only usable by players.", context.getSource().getSender());
            return 1;
        }

        NamespacedKey definitionKey = configuredArgumentMap.get(DEFINITION);

        if (definitionKey == null) {
            sendSimpleArgumentUsage(context);
            return 1;
        }

        SpellDefinition spell = WandcraftRegistries.SPELLS.get(definitionKey);

        if (spell == null) {
            plugin.sendMessage("Invalid spell definition: " + definitionKey.asString() + ".", context.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        }

        WbsWandcraft.getInstance().buildMessage("Casting ")
                .append(spell.displayName())
                .build()
                .send(player);

        new CastingQueue(new SpellInstance(spell), null).startCasting(player);

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        return sendSimpleArgumentUsage(context);
    }
}
