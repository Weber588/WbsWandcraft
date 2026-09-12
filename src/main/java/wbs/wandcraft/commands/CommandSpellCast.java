package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastingManager;
import wbs.wandcraft.context.CastingQueue;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;

public class CommandSpellCast extends WbsSubcommand {
    public CommandSpellCast(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    protected void addThens(LiteralArgumentBuilder<CommandSourceStack> builder) {
        for (SpellDefinition spell : WandcraftRegistries.SPELLS.values()) {
            String spellKey = spell.key().asString();
            LiteralCommandNode<CommandSourceStack> spellArg = Commands.literal(spellKey)
                    .executes(context -> cast(spell, context))
                    .build();

            for (SpellAttribute<?> attribute : spell.getAttributes()) {
                String attrKey = attribute.key().asString();
                LiteralArgumentBuilder<CommandSourceStack> attributeNode = Commands.literal(attrKey)
                        .executes(context -> {
                            plugin.sendMessage("Enter a value! (jane pls make this better)", context.getSource().getSender());
                            return Command.SINGLE_SUCCESS;
                        }).then(Commands.argument("attribute-" + attrKey, attribute.getArgumentType())
                                .suggests(attribute)
                                .executes(context -> cast(spell, context))
                                .redirect(spellArg, RedirectedStack::new)
                        );

                spellArg.addChild(attributeNode.build());
            }

            builder.then(spellArg);
        }
    }

    private int cast(SpellDefinition spell, CommandContext<CommandSourceStack> context) {
        plugin.getLogger().info("source class " + context.getSource().getClass().getSimpleName());
        CommandSender sender = context.getSource().getSender();

        if (!(sender instanceof Player player)) {
            plugin.sendMessage("This command is only usable by players.", sender);
            return Command.SINGLE_SUCCESS;
        }

        SpellInstance instance = new SpellInstance(spell);

        RedirectedStack.loopParents(context, c -> {
            for (SpellAttribute<?> attribute : spell.getAttributes()) {
                try {
                    String argName = "attribute-" + attribute.key().asString();
                    SpellAttributeInstance<?> attrInstance = attribute.getInstance(c, argName);
                    instance.setAttribute(attrInstance);
                } catch (IllegalArgumentException ignored) {}
            }
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
