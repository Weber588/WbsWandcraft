package wbs.wandcraft.commands;

import com.google.common.collect.Multimap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.WbsRegistry;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.generation.SpellInstanceGenerator;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.learning.LearningMethod;
import wbs.wandcraft.spell.learning.RegistrableLearningMethod;
import wbs.wandcraft.util.MenuUtils;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CommandInfoSpell extends CommandInfo<SpellDefinition> {
    private static final WbsSimpleArgument.KeyedSimpleArgument DEFINITION = new WbsSimpleArgument.KeyedSimpleArgument(
            "definition",
            WbsWandcraft.getInstance(),
            null
    ).setKeyedSuggestions(WandcraftRegistries.SPELLS.values());

    public static Component getSpellPage(SpellDefinition spell, boolean isKnown, boolean collapse) {
        Component types = spell.getTypesDisplay();

        Component attributeComponent = getAttributeComponent(spell, collapse);
        Component learningComponent = getLearningComponent(spell, collapse);
        Component generationComponent = getGenerationComponent(spell, collapse);

        WbsMessageBuilder builder = WbsWandcraft.getInstance().buildMessageNoPrefix(spell.displayName());

        if (isKnown) {
            int cost = spell.getEchoShardCost();
            builder.append(Component.text(" (" + cost + ")")
                    .style(MenuUtils.COST_STYLE)
                    .hoverEvent(HoverEvent.showText(Component.text("Costs " + cost + " echo shards to craft").style(MenuUtils.COST_STYLE)))
            );
        }

        if (collapse) {
            builder.append(" ").append(attributeComponent);
            if (learningComponent != null) {
                builder.append(learningComponent);
            }
            if (generationComponent != null) {
                builder.append(generationComponent);
            }
        }

        builder.append("\n").append(types);

        if (spell instanceof CastableSpell castableSpell && castableSpell.requiresConcentration()) {
            builder.append(Component.text("\nConcentration").style(MenuUtils.EXTRAS_STYLE).hoverEvent(HoverEvent.showText(
                    Component.text("""
                            This spell requires concentration for
                            the duration of the spell.
                            You can only concentrate on a single
                            spell at a time.""").style(MenuUtils.DESCRIPTION_STYLE)
            )));
        }

        Component description = spell.description();

        if (!isKnown) {
            description = description.font(Key.key("illageralt"))
                    .decorate(TextDecoration.ITALIC);
        }

        builder.append(MenuUtils.LINE_BREAK)
                .append(description.applyFallbackStyle(MenuUtils.DESCRIPTION_STYLE));

        if (!collapse) {
            builder.append(Component.newline()).append(attributeComponent);

            // TODO: Show triggered events? Sorted by spell aspect?

            if (learningComponent != null) {
                builder.append(Component.newline()).append(learningComponent);
            }

            if (generationComponent != null) {
                builder.append(Component.newline()).append(generationComponent);
            }
        }

        return builder.toComponent();
    }

    public CommandInfoSpell(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label, WandcraftRegistries.SPELLS);
        
        this.addSimpleArgument(DEFINITION);
    }

    protected @NonNull List<SpellDefinition> getEntries(WbsRegistry<SpellDefinition> registry) {
        return registry.stream().sorted(Comparator.comparing(Keyed::key)).toList();
    }

    protected Component getComponent(SpellDefinition spell, boolean collapse) {
        // TODO: Make it configurable for if the command can force isKnown
        return getSpellPage(spell, true, collapse);
    }

    @Override
    protected @Nullable SpellDefinition getT(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        NamespacedKey definitionKey = configuredArgumentMap.get(DEFINITION);

        CommandSender sender = context.getSource().getSender();
        if (definitionKey == null) {
            plugin.sendMessage("Choose a spell: "
                            + WandcraftRegistries.SPELLS.stream()
                            .map(Keyed::key)
                            .map(Key::asString)
                            .collect(Collectors.joining(", ")),
                    sender);
            return null;
        }

        SpellDefinition spell = WandcraftRegistries.SPELLS.get(definitionKey);

        if (spell == null) {
            plugin.sendMessage("Invalid spell definition: " + definitionKey.asString() + ".", sender);
            return null;
        }
        return spell;
    }

    private static @Nullable Component getGenerationComponent(SpellDefinition spell, boolean collapse) {
        List<RegistrableLearningMethod> generationMethods = WbsWandcraft.getInstance().getSettings()
                .getGenerationMethods()
                .stream()
                .filter(method -> {
                    if (method.getResultGenerator() instanceof SpellInstanceGenerator generator) {
                        return generator.getSpells().contains(spell);
                    }
                    return false;
                }).toList();

        if (generationMethods.isEmpty()) {
            return null;
        }

        Component indent = Component.text("  ");
        Component generation = Component.join(
                JoinConfiguration.newlines(),
                generationMethods.stream().map(method -> {
                    Component component = Component.empty();
                    if (!collapse) {
                        component = component.append(indent);
                    }
                    return component.append(
                            method.describe(indent).style(MenuUtils.EXTRAS_STYLE)
                    );
                }).toList()
        );

        TextComponent textDescription = Component.text("Generation: \n").style(MenuUtils.EXTRAS_STYLE)
                .append(generation);

        if (collapse) {
            return Component.text("[G]")
                    .style(MenuUtils.EXTRAS_STYLE)
                    .hoverEvent(HoverEvent.showText(textDescription));
        } else {
            return textDescription;
        }
    }

    private static @Nullable TextComponent getLearningComponent(SpellDefinition spell, boolean collapse) {
        Multimap<SpellDefinition, LearningMethod> learningMap = WbsWandcraft.getInstance().getSettings().getLearningMap();

        Collection<LearningMethod> methodList = learningMap.get(spell);
        Component indent = Component.text("  ");
        Component learning = Component.join(
                JoinConfiguration.newlines(),
                methodList.stream()
                        .map(criteria -> {
                            TextComponent component = Component.empty();
                            if (!collapse) {
                                component = component.append(indent);
                            }
                            return component.append(criteria.describe(indent).style(MenuUtils.EXTRAS_STYLE));
                        }).toList()
        );

        if (methodList.isEmpty()) {
            return null;
        }

        TextComponent descriptionText = Component.text("Learning criteria: \n").style(MenuUtils.EXTRAS_STYLE)
                .append(learning);

        TextComponent text;
        if (collapse) {
            text = Component.text("[L]")
                    .style(MenuUtils.EXTRAS_STYLE)
                    .hoverEvent(HoverEvent.showText(descriptionText));
        } else {
            text = descriptionText;
        }
        return text;
    }

    private static @NonNull Component getAttributeComponent(SpellDefinition spell, boolean collapse) {
        Component attributes = Component.join(JoinConfiguration.newlines(), spell.getLore());
        TextComponent descriptionText = Component.text("Attributes: \n")
                .style(MenuUtils.EXTRAS_STYLE)
                .append(attributes);

        if (collapse) {
            return Component.text("[A]")
                    .style(MenuUtils.EXTRAS_STYLE)
                    .hoverEvent(HoverEvent.showText(descriptionText));
        } else {
            return descriptionText;
        }
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        plugin.sendMessage("Usage: &h/" + context.getInput() + " <spell>", context.getSource().getSender());
        return Command.SINGLE_SUCCESS;
    }
}
