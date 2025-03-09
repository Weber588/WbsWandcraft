package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument.KeyedSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.ItemDecorator;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.util.CustomPersistentDataTypes;

@SuppressWarnings("UnstableApiUsage")
public class CommandBuildSpell extends WbsSubcommand {
    private static final KeyedSimpleArgument DEFINITION = new KeyedSimpleArgument(
            "definition",
            ArgumentTypes.namespacedKey(),
            WbsWandcraft.getKey("invalid")
    ).addKeyedSuggestions(WandcraftRegistries.SPELLS.values());
    private static final WbsSimpleArgument<String> ATTRIBUTES_STRING = new WbsSimpleArgument<>(
            "attributes",
            StringArgumentType.greedyString(),
            "",
            String.class
    ).setSuggestionProvider((context, builder) -> {
        NamespacedKey definitionKey = DEFINITION.getValue(context);
        // TODO: Suggest attributes (and parse in execute below)
        return builder.buildFuture();
    });

    public CommandBuildSpell(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
        addSimpleArgument(DEFINITION);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        NamespacedKey definitionKey = configuredArgumentMap.get(DEFINITION);
        SpellDefinition spell = WandcraftRegistries.SPELLS.get(definitionKey);

        if (spell == null) {
            return Command.SINGLE_SUCCESS;
        }

        ItemStack spellItem = ItemStack.of(Material.FLOW_BANNER_PATTERN);
        SpellInstance spellInstance = new SpellInstance(spell);

        // TODO: Add way to add attributes via command

        spellItem.editMeta(meta -> {
            for (ItemFlag value : ItemFlag.values()) {
                meta.addItemFlags(value);
            }

            meta.getPersistentDataContainer().set(SpellInstance.SPELL_INSTANCE_KEY, CustomPersistentDataTypes.SPELL_INSTANCE, spellInstance);
            ItemDecorator.decorate(spellInstance, meta);
        });

        if (context.getSource().getSender() instanceof Player player) {
            player.getInventory().addItem(spellItem);
        }

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> commandContext) {
        return 0;
    }
}
