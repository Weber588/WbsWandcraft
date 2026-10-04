package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.dynamic.DynamicSpell;
import wbs.wandcraft.util.ItemUtils;

public class CommandSpellBuildDynamic extends WbsSubcommand implements CommandDynamicSpell {
    public CommandSpellBuildDynamic(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);

        this.addSimpleArgument(ASPECT);
        this.addSimpleArgument(SPELL_TYPE);
        this.addSimpleArgument(SECONDARY_SPELL_TYPE);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        if (!(context.getSource().getSender() instanceof Player player)) {
            plugin.sendMessage("This command is only usable by players.", context.getSource().getSender());
            return 1;
        }

        DynamicSpell built = getDynamicSpell(context, configuredArgumentMap);
        if (built == null) return Command.SINGLE_SUCCESS;

        ItemStack item = ItemUtils.buildSpell(built);

        player.getInventory().addItem(item);
        WbsWandcraft.getInstance().buildMessage("Got 1 ")
                .append(item.effectiveName())
                .build()
                .send(player);

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        return sendSimpleArgumentUsage(context);
    }
}
