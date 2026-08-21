package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.context.CastingManager;

import java.util.List;

public class CommandSpellCancel extends WbsSubcommand {

    public static final String ARG_ENTITY = "entity";

    public CommandSpellCancel(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    protected void addThens(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.executes(context -> {
            CommandSender sender = context.getSource().getSender();

            if (sender instanceof Entity entity) {
                return cancel(sender, List.of(entity));
            }

            plugin.sendMessage("This command is only usable by entities.", sender);
            return 0;
        }).then(Commands.argument(ARG_ENTITY, ArgumentTypes.entities())
                .requires(source -> permission != null ? source.getSender().hasPermission(permission + ".other") : source.getSender().isOp())
                .executes(context -> {
                    CommandSender sender = context.getSource().getSender();

                    EntitySelectorArgumentResolver entityResolver = context.getArgument(ARG_ENTITY, EntitySelectorArgumentResolver.class);

                    List<Entity> entities;
                    try {
                        entities = entityResolver.resolve(context.getSource());
                    } catch (CommandSyntaxException e) {
                        throw new RuntimeException(e);
                    }

                    return cancel(sender, entities);
                })
        );
    }

    private int cancel(CommandSender sender, List<Entity> entities) {
        List<LivingEntity> targets = entities.stream()
                .filter(LivingEntity.class::isInstance)
                .map(LivingEntity.class::cast)
                .toList();

        if (targets.isEmpty()) {
            plugin.sendMessage("No valid targets.", sender);
            return 0;
        }

        int interrupted = 0;
        for (LivingEntity target : targets) {
            boolean isInterrupted = false;
            if (CastingManager.isCasting(target)) {
                CastingManager.stopCasting(target);
                isInterrupted = true;
            }
            if (CastingManager.isConcentrating(target)) {
                CastingManager.stopConcentrating(target);
                isInterrupted = true;
            }
            if (isInterrupted) {
                interrupted++;
            }
        }

        if (interrupted == 0) {
            plugin.sendMessage("Not casting/concentrating.", sender);
        } else {
            if (targets.size() == 1 && targets.getFirst().equals(sender)) {
                plugin.sendMessage("Stopped casting/concentrating.", sender);
            } else {
                plugin.sendMessage("Stopped %d entities from casting/concentrating.".formatted(interrupted), sender);
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        return sendSimpleArgumentUsage(context);
    }
}
