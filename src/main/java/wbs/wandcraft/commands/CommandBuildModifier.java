package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsEnumArgumentType;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.spell.modifier.ModifierScope;
import wbs.wandcraft.util.ItemUtils;

@SuppressWarnings("UnstableApiUsage")
public class CommandBuildModifier extends WbsSubcommand {
    private static final WbsSimpleArgument<ModifierScope> SCOPE = new WbsSimpleArgument<>(
            "modifier_scope",
            new WbsEnumArgumentType<>(ModifierScope.class),
            ModifierScope.NEXT,
            ModifierScope.class
    ).addSuggestions(ModifierScope.values());

    public CommandBuildModifier(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
        addSimpleArgument(SCOPE);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        ModifierScope scope = configuredArgumentMap.get(SCOPE);

        ItemStack item = ItemUtils.buildModifier(scope);

        if (context.getSource().getSender() instanceof Player player) {
            player.getInventory().addItem(item);
        }

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        return sendSimpleArgumentUsage(context);
    }
}
