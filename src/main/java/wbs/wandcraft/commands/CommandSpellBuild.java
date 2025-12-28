package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument.KeyedSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.util.ItemUtils;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class CommandSpellBuild extends WbsSubcommand {
    private static final KeyedSimpleArgument DEFINITION = new KeyedSimpleArgument(
            "definition",
            ArgumentTypes.namespacedKey(),
            null
    ).setKeyedSuggestions(WandcraftRegistries.SPELLS.values());

    public CommandSpellBuild(@NotNull WbsPlugin plugin, @NotNull String label) {
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
            List<SpellDefinition> list = WandcraftRegistries.SPELLS.stream().toList();
            if (list.size() > 54) {
                plugin.sendMessage("&wWarning! Not all spells in menu.", context.getSource().getSender());
                plugin.sendMessage("Choose a spell: "
                                + WandcraftRegistries.SPELLS.stream()
                                .map(Keyed::key)
                                .map(Key::asString)
                                .collect(Collectors.joining(", ")),
                        context.getSource().getSender());
            }

            Inventory inventory = Bukkit.createInventory(null, 6 * 9, Component.text("Spells"));

            for (int i = 0; i < Math.min(inventory.getSize(), list.size()); i++) {
                inventory.setItem(i, ItemUtils.buildSpell(list.get(i)));
            }

            player.openInventory(inventory);

            return Command.SINGLE_SUCCESS;
        }

        SpellDefinition spell = WandcraftRegistries.SPELLS.get(definitionKey);

        if (spell == null) {
            plugin.sendMessage("Invalid spell definition: " + definitionKey.asString() + ".", context.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        }

        ItemStack item = ItemUtils.buildSpell(spell);

        player.getInventory().addItem(item);

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        return sendSimpleArgumentUsage(context);
    }
}
