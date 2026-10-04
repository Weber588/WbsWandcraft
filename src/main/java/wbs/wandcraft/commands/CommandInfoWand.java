package wbs.wandcraft.commands;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.util.MenuUtils;
import wbs.wandcraft.wand.types.WandType;

import java.util.stream.Collectors;

public class CommandInfoWand extends CommandInfo<WandType<?>> {
    protected static final WbsSimpleArgument.KeyedSimpleArgument WAND_TYPE = new WbsSimpleArgument.KeyedSimpleArgument(
            "wand_type",
            WbsWandcraft.getInstance(),
            null
    ).addKeyedSuggestions(WandcraftRegistries.WAND_TYPES.values());

    public CommandInfoWand(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label, WandcraftRegistries.WAND_TYPES);
        
        this.addSimpleArgument(WAND_TYPE);
    }

    @Override
    protected Component getComponent(@UnknownNullability WandType<?> type, boolean collapse) {
        WbsMessageBuilder builder = plugin.buildMessageNoPrefix("")
                .append(type.getItemName().style(MenuUtils.DEFAULT_TITLE_STYLE))
                .append(MenuUtils.LINE_BREAK)
                .append(type.getDescription().applyFallbackStyle(MenuUtils.DESCRIPTION_STYLE));

        return builder.toComponent();
    }

    @Override
    protected @Nullable WandType<?> getT(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        NamespacedKey definitionKey = configuredArgumentMap.get(WAND_TYPE);
        CommandSender sender = context.getSource().getSender();

        if (definitionKey == null) {
            plugin.sendMessage("Choose a wand type: "
                            + WandcraftRegistries.WAND_TYPES.stream()
                            .map(Keyed::key)
                            .map(Key::asString)
                            .collect(Collectors.joining(", ")),
                    sender);
            return null;
        }

        WandType<?> type = WandcraftRegistries.WAND_TYPES.get(definitionKey);

        if (type == null) {
            plugin.sendMessage("Invalid wand definition: " + definitionKey.asString() + ".", sender);
            return null;
        }

        return type;
    }
}
