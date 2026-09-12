package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.RedirectedStack;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsKeyedArgumentType;
import wbs.utils.util.commands.brigadier.argument.WbsStringArgumentType;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastingManager;
import wbs.wandcraft.context.CastingQueue;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;

import java.util.List;

public class CommandSpellCast extends WbsSubcommand {
    public CommandSpellCast(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    protected void addThens(LiteralArgumentBuilder<CommandSourceStack> builder) {
        WbsKeyedArgumentType<SpellDefinition> spellArgType = new WbsKeyedArgumentType<>("spell", WandcraftRegistries.SPELLS);

        CommandNode<CommandSourceStack> spellArg = Commands.argument("spell", spellArgType)
                .executes(this::cast)
                .build();

        WbsKeyedArgumentType<SpellAttribute<?>> attributeArgType = new WbsKeyedArgumentType<>("attribute", WandcraftRegistries.ATTRIBUTES);

        CommandNode<CommandSourceStack> attributeArg = Commands.argument("attribute", attributeArgType)
                .executes(this::cast)
                .executes(context -> {
                    plugin.sendMessage("Enter a value! (jane pls make this better)", context.getSource().getSender());
                    return Command.SINGLE_SUCCESS;
                }).then(Commands.argument("attribute-value", WbsStringArgumentType.word())
                        .suggests((context, suggestions) -> {
                            SpellAttribute<?> attribute = context.getArgument("attribute", SpellAttribute.class);

                            return attribute.getSuggestions(context, suggestions);
                        })
                        .executes(this::cast)
                        .fork(spellArg, context -> List.of(new RedirectedStack(context)))
                ).build();

        spellArg.addChild(attributeArg);

        builder.then(spellArg);
    }

    private int cast(CommandContext<CommandSourceStack> context) {
        CommandSender sender = context.getSource().getSender();

        if (!(sender instanceof Player player)) {
            plugin.sendMessage("This command is only usable by players.", sender);
            return Command.SINGLE_SUCCESS;
        }

        SpellDefinition spell = RedirectedStack.getRoot(context).getArgument("spell", SpellDefinition.class);
        SpellInstance instance = new SpellInstance(spell);

        RedirectedStack.loopParents(context, c -> {
            try {
                SpellAttribute<?> attribute = c.getArgument("attribute", SpellAttribute.class);
                String attributeValueString = c.getArgument("attribute-value", String.class);

                WbsWandcraft.getInstance().getLogger().info("attribute: " + attribute.key().asString());
                WbsWandcraft.getInstance().getLogger().info("attribute-value: " + attributeValueString);

                SpellAttributeInstance<?> attributeInstance = attribute.getParsedInstance(attributeValueString);
                instance.setAttribute(attributeInstance);
            } catch (IllegalArgumentException ignored) {}
        });

        if (CastingManager.isCasting(player)) {
            plugin.buildMessage(
                    MiniMessage.miniMessage().deserialize("You are already casting a spell! Cancel it with ")
            ).send(player);
            return Command.SINGLE_SUCCESS;
        }

        WbsWandcraft.getInstance().buildMessage("Casting ")
                .append(spell.displayName())
                .build()
                .send(player);

        new CastingQueue(instance, null)
                .startCasting(player);

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        return sendSimpleArgumentUsage(context);
    }
}
