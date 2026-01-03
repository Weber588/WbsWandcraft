package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.util.ItemUtils;

@SuppressWarnings("UnstableApiUsage")
public class CommandSpellbookBuild extends WbsSubcommand {
    public CommandSpellbookBuild(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        if (sender instanceof Player player) {
            ItemStack item = ItemUtils.buildSpellbook();
            player.getInventory().addItem(item);
        } else {
            plugin.sendMessage("This command is only usable by players", sender);
            return 1;
        }

        return Command.SINGLE_SUCCESS;
    }
}
