package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsEnumArgumentType;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.cost.PlayerMana;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class CommandPlayerModify extends WbsSubcommand {
    private static final WbsSimpleArgument<PlayerSelectorArgumentResolver> PLAYER = new WbsSimpleArgument<>("player",
            ArgumentTypes.player(),
            null,
            PlayerSelectorArgumentResolver.class
    ).setSuggestionProvider(ArgumentTypes.player());
    private static final WbsSimpleArgument<ModifiableFeature> MODIFICATION_TARGET = new WbsSimpleArgument<>("modification_target",
            new WbsEnumArgumentType<>(ModifiableFeature.class),
            ModifiableFeature.MAX_MANA,
            ModifiableFeature.class
    ).addSuggestions(ModifiableFeature.values());
    private static final WbsSimpleArgument<Integer> VALUE = new WbsSimpleArgument<>("value",
            IntegerArgumentType.integer(0),
            null,
            Integer.class
    ).setSuggestionProvider((context, builder) -> {
        ModifiableFeature value = MODIFICATION_TARGET.getValue(context);

        if (value != null) {
            builder.suggest(value.defaultValue());
        }

        return builder.buildFuture();
    });

    public CommandPlayerModify(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);

        addSimpleArgument(PLAYER);
        addSimpleArgument(MODIFICATION_TARGET);
        addSimpleArgument(VALUE);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        CommandSender sender = context.getSource().getSender();

        PlayerSelectorArgumentResolver playerResolver = configuredArgumentMap.get(PLAYER);

        List<Player> resolved;
        try {
            resolved = playerResolver.resolve(context.getSource());
        } catch (CommandSyntaxException e) {
            plugin.sendMessage("&wFailed to parse player args!", sender);
            return Command.SINGLE_SUCCESS;
        }

        ModifiableFeature feature = configuredArgumentMap.get(MODIFICATION_TARGET);
        Integer value = configuredArgumentMap.get(VALUE);

        switch (feature) {
            // TODO: Make these send proper summary messages, instead of spamming for multiple players
            case MAX_MANA -> resolved.forEach(player -> {
                PlayerMana mana = new PlayerMana(player);
                if (value == null) {
                    plugin.buildMessage("")
                            .append(player.displayName())
                            .append(" has " + mana.getMana() + " mana, out of a possible " + mana.getMaxMana())
                            .send(sender);
                } else {
                    mana.showManaBar(player);
                    mana.setMaxMana(value);
                    mana.saveTo(player);
                    plugin.buildMessage("Set ")
                            .append(player.displayName())
                            .append("'s maximum mana to " + mana.getMaxMana())
                            .send(sender);
                }
            });
            case MANA_REGENERATION -> resolved.forEach(player -> {
                PlayerMana mana = new PlayerMana(player);
                if (value == null) {
                    plugin.buildMessage("")
                            .append(player.displayName())
                            .append(" has a mana regeneration rate of " + mana.getManaRegenerationRate())
                            .send(sender);
                } else {
                    mana.setManaRegenerationRate(value);
                    mana.saveTo(player);
                    plugin.buildMessage("Set ")
                            .append(player.displayName())
                            .append("'s mana regeneration rate to " + mana.getManaRegenerationRate())
                            .send(sender);
                }
            });
            case null -> plugin.sendMessage("&wInvalid feature -- choose from the following: " +
                    Arrays.stream(ModifiableFeature.values())
                            .map(Enum::name)
                            .collect(Collectors.joining(", ")),
                    sender
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        return sendSimpleArgumentUsage(context);
    }

    private enum ModifiableFeature {
        MAX_MANA(PlayerMana.DEFAULT_MAX_MANA),
        MANA_REGENERATION(PlayerMana.DEFAULT_MANA_REGENERATION),
        ;

        private final int defaultValue;

        ModifiableFeature(int defaultValue) {
            this.defaultValue = defaultValue;
        }

        public int defaultValue() {
            return defaultValue;
        }
    }
}
