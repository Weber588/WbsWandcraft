package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Keyed;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.WbsRegistry;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.util.MenuUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class CommandInfo<T extends Keyed> extends WbsSubcommand {
    protected final Map<T, Integer> pageNumbers = new HashMap<>();
    private final WbsRegistry<T> registry;
    protected Book infoBook;

    public CommandInfo(@NotNull WbsPlugin plugin, @NotNull String label, WbsRegistry<T> registry) {
        super(plugin, label);

        this.registry = registry;

        infoBook = buildBook();
    }

    private Book buildBook() {
        List<T> entries = getEntries(registry);
        List<Component> pages = new LinkedList<>();
        for (int i = 0; i < entries.size(); i++) {
            T type = entries.get(i);

            pages.add(getComponent(type, true));
            pageNumbers.put(type, i);
        }

        return Book.book(
                Component.text("WbsWandcraft"),
                Component.text("??? ???????").decorate(TextDecoration.OBFUSCATED),
                pages);
    }

    protected @NonNull List<T> getEntries(WbsRegistry<T> registry) {
        return this.registry.stream().toList();
    }

    protected abstract Component getComponent(@UnknownNullability T T, boolean collapse);

    @Override
    protected final int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        T type = getT(context, configuredArgumentMap);
        if (type == null) return Command.SINGLE_SUCCESS;

        CommandSender sender = context.getSource().getSender();

        int pageNum = pageNumbers.get(type);

        if (sender instanceof Player player) {
            MenuUtils.showBook(player, infoBook, pageNum);
        } else {
            sender.sendMessage(getComponent(type, false));
        }

        return Command.SINGLE_SUCCESS;
    }

    protected abstract @Nullable T getT(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap);

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        plugin.sendMessage("Usage: &h/" + context.getInput() + " <type>", context.getSource().getSender());
        return Command.SINGLE_SUCCESS;
    }
}
