package wbs.wandcraft.commands;

import com.google.common.collect.Multimap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.generation.SpellInstanceGenerator;
import wbs.wandcraft.learning.LearningMethod;
import wbs.wandcraft.learning.RegistrableLearningMethod;
import wbs.wandcraft.spell.definitions.SpellDefinition;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class CommandSpellInfo extends WbsSubcommand {
    private static final WbsSimpleArgument.KeyedSimpleArgument DEFINITION = new WbsSimpleArgument.KeyedSimpleArgument(
            "definition",
            ArgumentTypes.namespacedKey(),
            null
    ).setKeyedSuggestions(WandcraftRegistries.SPELLS.values());

    public CommandSpellInfo(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
        
        this.addSimpleArgument(DEFINITION);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        NamespacedKey definitionKey = configuredArgumentMap.get(DEFINITION);

        if (definitionKey == null) {
            plugin.sendMessage("Choose a spell: "
                            + WandcraftRegistries.SPELLS.stream()
                            .map(Keyed::key)
                            .map(Key::asString)
                            .collect(Collectors.joining(", ")),
                    context.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        }

        SpellDefinition spell = WandcraftRegistries.SPELLS.get(definitionKey);

        if (spell == null) {
            plugin.sendMessage("Invalid spell definition: " + definitionKey.asString() + ".", context.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        }

        Component types = spell.getTypesDisplay();
        WbsMessageBuilder builder = plugin.buildMessageNoPrefix("=======================")
                .append("\nSpell: ")
                .append(spell.displayName().color(NamedTextColor.GOLD))
                .append("\nTypes: ")
                .append(types)
                .append("\nDescription: ")
                .append(spell.description().color(NamedTextColor.GOLD))
                .append("\nAttributes: ");

        spell.getLore().forEach(text -> {
            builder.append("\n")
                    .append(text);
        });

        Multimap<SpellDefinition, LearningMethod> learningMap = WbsWandcraft.getInstance().getSettings().getLearningMap();

        Collection<LearningMethod> methodList = learningMap.get(spell);
        if (!methodList.isEmpty()) {
            builder.append("\nLearning criteria:");
            Component indent = Component.text("  ");
            methodList.forEach(criteria ->
                    builder.append(Component.newline().append(indent).append(criteria.describe(indent).color(NamedTextColor.GOLD)))
            );
        }

        List<RegistrableLearningMethod> generationMethods = WbsWandcraft.getInstance().getSettings().getGenerationMethods();

        if (!generationMethods.isEmpty()) {
            builder.append("\nGeneration:");
            Component indent = Component.text("  ");
            generationMethods.forEach(method -> {
                if (method.getResultGenerator() instanceof SpellInstanceGenerator generator) {
                    if (generator.getSpells().contains(spell)) {
                        builder.append(Component.newline().append(indent).append(method.describe(indent).color(NamedTextColor.GOLD)));
                    }
                }
            });
        }

        builder
                .append("\n=======================")
                .send(context.getSource().getSender());

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        plugin.sendMessage("Usage: &h/" + context.getInput() + " <spell>", context.getSource().getSender());
        return Command.SINGLE_SUCCESS;
    }
}
