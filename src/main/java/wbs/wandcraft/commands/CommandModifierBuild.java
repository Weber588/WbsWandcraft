package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.util.ItemUtils;

@SuppressWarnings("UnstableApiUsage")
public class CommandModifierBuild extends WbsSubcommand {
    public CommandModifierBuild(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        ItemStack item = ItemUtils.buildModifier();

        if (context.getSource().getSender() instanceof Player player) {
            player.getInventory().addItem(item);
        }

        return Command.SINGLE_SUCCESS;
    }
}
