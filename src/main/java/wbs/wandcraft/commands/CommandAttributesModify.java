package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.KeyedSuggestionProvider;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.WbsSuggestionProvider;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument.KeyedSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.spell.attributes.Attributable;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.attributes.modifier.AttributeAddModifierType;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.modifier.SpellModifier;
import wbs.wandcraft.wand.Wand;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class CommandAttributesModify extends WbsSubcommand {
    private static final KeyedSimpleArgument ATTRIBUTE_KEY = new KeyedSimpleArgument("attribute_key",
            ArgumentTypes.namespacedKey(),
            null
    );
    private static final WbsSimpleArgument<String> ATTRIBUTE_VALUE = new WbsSimpleArgument<>("attribute_value",
            StringArgumentType.word(),
            "",
            String.class
    );
    private static final KeyedSimpleArgument MODIFIER_TYPE = new KeyedSimpleArgument("modifier_type",
            ArgumentTypes.namespacedKey(),
            AttributeAddModifierType.KEY
    );

    static {
        ATTRIBUTE_KEY.setSuggestionProvider((context, builder) -> {
            CommandSender sender = context.getSource().getSender();
            if (sender instanceof Player player) {
                ItemStack item = player.getInventory().getItemInMainHand();

                Attributable attributable = null;
                Wand wand = Wand.getIfValid(item);
                SpellInstance instance = SpellInstance.fromItem(item);
                if (wand != null) {
                    attributable = wand;
                } else if (instance != null) {
                    attributable = instance;
                }

                List<SpellAttribute<?>> attributes = new LinkedList<>();

                if (attributable != null) {
                    attributable.getAttributeValues().stream()
                            .map(SpellAttributeInstance::attribute)
                            .forEach(attributes::add);
                } else {
                    attributes.addAll(WandcraftRegistries.ATTRIBUTES.values());
                }

                return WbsSuggestionProvider.getStatic(
                                attributes.stream()
                                        .map(Keyed::key)
                                        .map(Key::asString)
                                        .toList())
                        .getSuggestions(context, builder);
            }
            return builder.buildFuture();
        });

        ATTRIBUTE_VALUE.setSuggestionProvider((context, builder) -> {
            NamespacedKey attributeKey = ATTRIBUTE_KEY.getValue(context.getLastChild());
            SpellAttribute<?> attribute = WandcraftRegistries.ATTRIBUTES.get(attributeKey);

            if (attribute != null) {
                return WbsSuggestionProvider.getStatic(attribute.getSuggestions().stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .toList()).getSuggestions(context, builder);
            } else {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherParseException().create("Attribute not found: " + attributeKey.asString());
            }
        });

        MODIFIER_TYPE.setSuggestionProvider((context, builder) -> {
            if (context.getSource().getSender() instanceof Player player) {
                ItemStack held = player.getInventory().getItemInMainHand();

                if (SpellModifier.fromItem(held) != null) {
                    return KeyedSuggestionProvider.getStaticKeyed(WandcraftRegistries.MODIFIER_TYPES.values()).getSuggestions(context, builder);
                }
            }

            return builder.buildFuture();
        });
    }

    public CommandAttributesModify(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
        addSimpleArgument(ATTRIBUTE_KEY);
        addSimpleArgument(ATTRIBUTE_VALUE);
        addSimpleArgument(MODIFIER_TYPE);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        NamespacedKey key = configuredArgumentMap.get(ATTRIBUTE_KEY);
        SpellAttribute<?> attribute = WandcraftRegistries.ATTRIBUTES.get(key);

        CommandSender sender = context.getSource().getSender();
        if (attribute == null) {
            plugin.sendMessage("Invalid attribute: " + key.asString() + ".", sender);
            return Command.SINGLE_SUCCESS;
        }

        if (!(sender instanceof Player player)) {
            plugin.sendMessage("This command is only usable by players.", sender);
            return Command.SINGLE_SUCCESS;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.isEmpty()) {
            plugin.sendMessage("Hold the item to modify!", sender);
            return Command.SINGLE_SUCCESS;
        }

        String stringValue = configuredArgumentMap.get(ATTRIBUTE_VALUE);
        SpellAttributeInstance<?> attributeInstance = attribute.getParsedInstance(stringValue);

        SpellModifier spellModifier = SpellModifier.fromItem(item);

        if (spellModifier != null) {
            updateModifier(item, sender, configuredArgumentMap, spellModifier, attributeInstance);
        } else {
            updateAttributable(item, sender, attributeInstance);
        }

        return Command.SINGLE_SUCCESS;
    }

    private void updateAttributable(ItemStack item, CommandSender sender, SpellAttributeInstance<?> attributeInstance) {
        Attributable attributable;
        Wand wand = Wand.getIfValid(item);
        SpellInstance instance = SpellInstance.fromItem(item);

        if (wand != null) {
            attributable = wand;
        } else if (instance != null) {
            attributable = instance;
        } else {
            plugin.sendMessage("The held item does not support attributes.", sender);
            return;
        }

        attributable.setAttribute(attributeInstance);

        if (wand != null) {
            wand.toItem(item);
            plugin.sendMessage("Updated wand!", sender);
        }
        if (instance != null) {
            instance.toItem(item);
            plugin.sendMessage("Updated spell!", sender);
        }
    }

    private void updateModifier(ItemStack item, CommandSender sender, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap, SpellModifier spellModifier, SpellAttributeInstance<?> attributeInstance) {
        NamespacedKey modifierTypeKey = configuredArgumentMap.get(MODIFIER_TYPE);
        if (modifierTypeKey == null) {
            plugin.sendMessage("Modifying a spell modifier requires a modifier type.", sender);
            return;
        }

        AttributeModifierType modifierType = WandcraftRegistries.MODIFIER_TYPES.get(modifierTypeKey);
        if (modifierType == null) {
            plugin.sendMessage("Invalid attribute modifier type: " + modifierTypeKey.asString(), sender);
            return;
        }

        SpellAttributeModifier<?> modifierInstance = new SpellAttributeModifier<>(attributeInstance, modifierType);

        spellModifier.getModifiers().forEach(modifier -> {
            if (modifier.attribute().equals(modifierInstance.attribute())) {
                // Safe to remove in loop -- getModifiers returns copy
                spellModifier.removeModifier(modifier);
            }
        });
        spellModifier.addModifier(modifierInstance);

        spellModifier.toItem(item);
        plugin.sendMessage("Updated spell modifier!", sender);
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> commandContext) {
        return 0;
    }
}
