package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastingQueue;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.dynamic.DynamicSpell;
import wbs.wandcraft.spell.dynamic.FixedDomainSpellArchetype;
import wbs.wandcraft.spell.dynamic.SpellArchetype;
import wbs.wandcraft.util.MenuUtils;

public class CommandSpellCastDynamic extends WbsSubcommand implements CommandDynamicSpell {
    static {
        SECONDARY_DOMAIN.setSuggestionProvider((context, builder) -> {
            SpellArchetype value = ARCHETYPE.getValue(context);

            // Don't suggest a 2nd type if a fixed aspect already has a first
            if (value instanceof FixedDomainSpellArchetype) {
                return builder.buildFuture();
            }

            return SECONDARY_DOMAIN.getSuggestions(context, builder);
        });
    }

    public CommandSpellCastDynamic(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);

        this.addSimpleArgument(ARCHETYPE);
        this.addSimpleArgument(MAGIC_DOMAIN);
        this.addSimpleArgument(SECONDARY_DOMAIN);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        CommandSender sender = context.getSource().getSender();
        if (!(sender instanceof Player player)) {
            plugin.sendMessage("This command is only usable by players.", sender);
            return Command.SINGLE_SUCCESS;
        }

        DynamicSpell built = getDynamicSpell(context, configuredArgumentMap);
        if (built == null) return Command.SINGLE_SUCCESS;

        SpellInstance instance = built.newInstance();

        Component attributesHover = Component.join(
                JoinConfiguration.newlines(),
                instance.deriveAttributeValues().stream()
                        .sorted()
                        .map(attr ->
                                (Component) Component.text("  - ")
                                        .style(MenuUtils.ACCENTS_STYLE)
                                        .append(attr.toComponent().style(MenuUtils.EXTRAS_STYLE))
                        )
                        .toList()
        );

        WbsWandcraft.getInstance().buildMessage("Casting ")
                .append(built.displayName().hoverEvent(HoverEvent.showText(attributesHover)))
                .build()
                .send(player);

        new CastingQueue(instance, null)
                .startCasting(player);

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        plugin.sendMessage("Usage: &h/" + context.getInput() + " <spell>", context.getSource().getSender());
        return Command.SINGLE_SUCCESS;
    }
}
