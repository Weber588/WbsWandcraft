package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.dialog.Dialog;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.spellbook.DialogSpellbook;

@SuppressWarnings("UnstableApiUsage")
public class CommandSpellbook extends WbsSubcommand {
    public CommandSpellbook(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();
        if (!(sender instanceof Player player)) {
            plugin.sendMessage("This command is only usable by players", sender);
            return 1;
        }

        Dialog dialog = DialogSpellbook.getDialog(player.getPersistentDataContainer());

        player.showDialog(dialog);

        return Command.SINGLE_SUCCESS;
    }
}
