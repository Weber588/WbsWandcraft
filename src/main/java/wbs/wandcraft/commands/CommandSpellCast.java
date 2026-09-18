package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
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
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class CommandSpellCast extends WbsSubcommand {
    public CommandSpellCast(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
    }

    @Override
    protected void addThens(LiteralArgumentBuilder<CommandSourceStack> builder) {
        for (SpellDefinition spell : WandcraftRegistries.SPELLS.values()) {
            String spellKey = spell.key().asString();

            CommandNode<CommandSourceStack> spellArg = Commands.literal(spellKey)
                    .executes(context -> cast(spell, context))
                    .build();

            Collection<SpellAttribute<?>> attributes = spell.getAttributes();
            attributes.remove(CastableSpell.COOLDOWN);
            attributes.remove(CastableSpell.COST);

            WbsKeyedArgumentType<SpellAttribute<?>> attributeArgType = new WbsKeyedArgumentType<>("attribute", WandcraftRegistries.ATTRIBUTES)
                    .defaultNamespace(WbsWandcraft.getInstance().namespace())
                    .setSuggestionProvider(context -> {
                        List<SpellAttribute<?>> toSuggest = new LinkedList<>(attributes);

                        getAttributes(spell, context).stream()
                                .map(SpellAttributeInstance::attribute)
                                .forEach(toSuggest::remove);

                        return toSuggest;
                    });

            CommandNode<CommandSourceStack> previous = spellArg;
            for (int i = 0; i < attributes.size(); i++) {
                int finalI = i;
                WbsStringArgumentType attrValueArgType = new WbsStringArgumentType.StringWordArgumentType() {
                    @Override
                    public @NotNull ArgumentType<String> getNativeType() {
                        return StringArgumentType.string();
                    }
                };
                ArgumentCommandNode<CommandSourceStack, String> attrValue = Commands.argument("attribute-value-" + i, attrValueArgType)
                        .suggests((context, suggestions) -> {
                            SpellAttribute<?> attribute = context.getArgument("attribute-" + finalI, SpellAttribute.class);

                            return attribute.listSuggestions(context, suggestions);
                        })
                        .executes(context -> cast(spell, context))
                        .build();

                CommandNode<CommandSourceStack> attributeArg = Commands.argument("attribute-" + i, attributeArgType)
                        .executes(context -> {
                            SpellAttribute<?> attribute = context.getArgument("attribute-" + finalI, SpellAttribute.class);
                            plugin.buildMessage("Enter a value for ")
                                    .append(attribute.displayName())
                                    .send(context.getSource().getSender());
                            return Command.SINGLE_SUCCESS;
                        }).then(attrValue)
                        .build();

                previous.addChild(attributeArg);
                previous = attrValue;
            }

            builder.then(spellArg);
        }
    }

    private int cast(SpellDefinition spell, CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSender sender = context.getSource().getSender();

        if (!(sender instanceof Player player)) {
            plugin.sendMessage("This command is only usable by players.", sender);
            return Command.SINGLE_SUCCESS;
        }

        SpellInstance instance = new SpellInstance(spell);

        getAttributes(spell, context).forEach(instance::setAttribute);

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

    private static List<SpellAttributeInstance<?>> getAttributes(SpellDefinition spell, CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        List<SpellAttributeInstance<?>> attributes = new LinkedList<>();
        for (int i = 0; i < spell.getAttributes().size(); i++) {
            SpellAttribute<?> attribute;
            String attributeValueString;

            try {
                attribute = context.getArgument("attribute-" + i, SpellAttribute.class);
                attributeValueString = context.getArgument("attribute-value-" + i, String.class);
            } catch (IllegalArgumentException ignored) {
                continue;
            }

            SpellAttributeInstance<?> attributeInstance;
            try {
                attributeInstance = attribute.getParsedInstance(attributeValueString);
                attributes.add(attributeInstance);
            } catch (NumberFormatException ex) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create(ex.getMessage());
            }
        }

        return attributes;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        return sendSimpleArgumentUsage(context);
    }
}
