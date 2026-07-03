package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastingManager;
import wbs.wandcraft.context.CastingQueue;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class CommandSpellCancel extends WbsSubcommand {
    private static final WbsSimpleArgument<EntitySelectorArgumentResolver> DEFINITION = new WbsSimpleArgument<>(
            "entity",
            ArgumentTypes.entities(),
            source -> {
                CommandSender sender = source.getSender();
                if (sender instanceof Entity entity) {
                    return List.of(entity);
                }
                return List.of();
            },
            EntitySelectorArgumentResolver.class
    );

    public CommandSpellCancel(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
        addSimpleArgument(DEFINITION);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        CommandSender sender = context.getSource().getSender();

        EntitySelectorArgumentResolver entityResolver = configuredArgumentMap.get(DEFINITION);

        List<Entity> entities;
        try {
            entities = entityResolver.resolve(context.getSource());
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }

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

        plugin.sendMessage("Interrupted %d entities casting/concentrating.".formatted(interrupted), sender);

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        return sendSimpleArgumentUsage(context);
    }
}
