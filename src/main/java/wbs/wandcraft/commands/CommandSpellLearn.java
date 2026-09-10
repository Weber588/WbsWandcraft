package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spellbook.Spellbook;

public class CommandSpellLearn extends SpellSubcommand {
    public CommandSpellLearn(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }


    @Override
    protected int runSpellCommand(CommandContext<CommandSourceStack> context, CommandSender sender, SpellDefinition spell) {
        if (!(sender instanceof Player player)) {
            plugin.sendMessage("This command is only usable by players", sender);
            return 1;
        }

        Spellbook.teachSpell(player, spell);

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int runSpellCommandAll(CommandContext<CommandSourceStack> context, CommandSender sender) {
        if (!(sender instanceof Player player)) {
            plugin.sendMessage("This command is only usable by players", sender);
            return 1;
        }

        Spellbook.teachSpells(player, WandcraftRegistries.SPELLS.stream().toList());

        return Command.SINGLE_SUCCESS;
    }
}
