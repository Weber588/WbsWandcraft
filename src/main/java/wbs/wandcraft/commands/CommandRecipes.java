package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.crafting.ArtificingRecipe;

import java.util.List;

public class CommandRecipes extends WbsSubcommand {
    public CommandRecipes(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();

        List<ArtificingRecipe> recipes = WbsWandcraft.getInstance().getSettings().getArtificingRecipes();

        if (recipes.isEmpty()) {
            plugin.sendMessage("No artificing recipes configured.", sender);
        } else {
            plugin.sendMessage("%d artificing recipes configured:".formatted(recipes.size()), sender);
            for (ArtificingRecipe recipe : recipes) {
                plugin.sendMessage(recipe.toString(), sender); // TODO: Move this away from toString
            }
        }

        return Command.SINGLE_SUCCESS;
    }
}
