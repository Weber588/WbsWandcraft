package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.generation.AttributeModifierGenerator;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.util.ItemUtils;

import java.util.stream.Collectors;

public class CommandModifierGenerate extends WbsSubcommand  {
    private static final WbsSimpleArgument.KeyedSimpleArgument MODIFIER_GENERATOR = new WbsSimpleArgument.KeyedSimpleArgument(
            "modifier_generator",
            ArgumentTypes.namespacedKey(),
            null
    ).addKeyedSuggestions(WandcraftRegistries.MODIFIER_GENERATORS.values());

    public CommandModifierGenerate(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
        addSimpleArgument(MODIFIER_GENERATOR);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        NamespacedKey generatorKey = configuredArgumentMap.get(MODIFIER_GENERATOR);

        if (generatorKey == null) {
            plugin.sendMessage("Choose a modifier generator: "
                            + WandcraftRegistries.MODIFIER_GENERATORS.stream()
                            .map(Keyed::key)
                            .map(Key::asString)
                            .collect(Collectors.joining(", ")),
                    context.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        }

        AttributeModifierGenerator<?> generator = WandcraftRegistries.MODIFIER_GENERATORS.get(generatorKey);

        if (generator == null) {
            plugin.sendMessage("Invalid generator key: " + generatorKey.asString() + ".", context.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        }

        if (context.getSource().getSender() instanceof Player player) {
            SpellAttributeModifier<?, ?> modifier = generator.get();

            player.getInventory().addItem(ItemUtils.buildModifier(modifier));
        }

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> commandContext) {
        return 0;
    }
}
