package wbs.wandcraft.commands;

import com.google.common.collect.Multimap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.WrittenBookContent;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MenuType;
import org.bukkit.inventory.view.LecternView;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.utils.util.pluginhooks.hooks.PacketEventsWrapper;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.generation.SpellInstanceGenerator;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.learning.LearningMethod;
import wbs.wandcraft.spell.learning.RegistrableLearningMethod;
import wbs.wandcraft.spellbook.Spellbook;
import wbs.wandcraft.util.ItemUtils;

import java.util.*;
import java.util.stream.Collectors;

public class CommandSpellInfo extends WbsSubcommand {
    private static final WbsSimpleArgument.KeyedSimpleArgument DEFINITION = new WbsSimpleArgument.KeyedSimpleArgument(
            "definition",
            WbsWandcraft.getInstance(),
            null
    ).setKeyedSuggestions(WandcraftRegistries.SPELLS.values());

    private static final TextColor CATEGORY_COLOR = TextColor.color(0xe3a11c);
    public static final TextColor ACCENTS_STYLE = NamedTextColor.GOLD;

    private static final Map<SpellDefinition, Integer> PAGE_NUMBERS = new HashMap<>();
    private static Book spellInfoBook;

    public CommandSpellInfo(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
        
        this.addSimpleArgument(DEFINITION);

        spellInfoBook = buildBook();
    }

    private Book buildBook() {
        List<SpellDefinition> spells = WandcraftRegistries.SPELLS.stream().sorted().toList();
        List<Component> pages = new LinkedList<>();
        for (int i = 0; i < spells.size(); i++) {
            SpellDefinition def = spells.get(i);

            pages.add(getComponent(def, true));
            PAGE_NUMBERS.put(def, i);
        }

        return Book.book(
                Component.text("WbsWandcraft Spells"),
                Component.text("??? ???????").decorate(TextDecoration.OBFUSCATED),
                pages);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        NamespacedKey definitionKey = configuredArgumentMap.get(DEFINITION);

        CommandSender sender = context.getSource().getSender();
        if (definitionKey == null) {
            plugin.sendMessage("Choose a spell: "
                            + WandcraftRegistries.SPELLS.stream()
                            .map(Keyed::key)
                            .map(Key::asString)
                            .collect(Collectors.joining(", ")),
                    sender);
            return Command.SINGLE_SUCCESS;
        }

        SpellDefinition spell = WandcraftRegistries.SPELLS.get(definitionKey);

        if (spell == null) {
            plugin.sendMessage("Invalid spell definition: " + definitionKey.asString() + ".", sender);
            return Command.SINGLE_SUCCESS;
        }

        int pageNum = PAGE_NUMBERS.get(spell);

        if (sender instanceof Player player) {
            // TODO: Move lectern page-specific viewing to a util class
            if (player.getGameMode() == GameMode.SURVIVAL) {
                PacketEventsWrapper.get().ifPresent(pe -> pe.sendGameModeChange(GameMode.ADVENTURE, player));
            }
            Location checkLoc = player.getLocation();
            checkLoc.setY(player.getWorld().getMaxHeight());
            while (checkLoc.getY() >= checkLoc.getWorld().getMinHeight() && !checkLoc.getBlock().isEmpty()) {
                checkLoc = checkLoc.add(0, -1, 0);
            }

            if (checkLoc.getY() < checkLoc.getWorld().getMinHeight()) {
                checkLoc = player.getLocation().add(0, 2, 0);
            }

            Location createLoc = checkLoc;
            LecternView lecternView = MenuType.LECTERN.builder().checkReachable(false).location(createLoc).build(player);
            ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
            item.setData(
                    DataComponentTypes.WRITTEN_BOOK_CONTENT, WrittenBookContent.writtenBookContent("", "")
                                    .addPages(spellInfoBook.pages())
                            .build()
            );
            item.editPersistentDataContainer(container -> {
                container.set(ItemUtils.WANDCRAFT_ITEM_KEY, PersistentDataType.STRING, "info_book");
            });
            lecternView.setPage(pageNum);
            lecternView.setItem(0, item);
            lecternView.open();

            createLoc.getBlock().setType(Material.AIR);

            WbsWandcraft.getInstance().runAtEndOfTick(() -> {
                lecternView.setPage(pageNum);
                createLoc.getBlock().setType(Material.AIR);
            });
        } else {
            sender.sendMessage(getComponent(spell, false));
        }

        return Command.SINGLE_SUCCESS;
    }

    private Component getComponent(SpellDefinition spell, boolean collapse) {
        Component types = spell.getTypesDisplay();

        Component attributeComponent = getAttributeComponent(spell, collapse);
        Component learningComponent = getLearningComponent(spell, collapse);
        Component generationComponent = getGenerationComponent(spell, collapse);

        WbsMessageBuilder builder = plugin.buildMessageNoPrefix(spell.displayName());

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
            builder.append(Component.text("\nConcentration").color(NamedTextColor.BLUE).hoverEvent(HoverEvent.showText(
                    Component.text("""
                            This spell requires concentration for
                            the duration of the spell.
                            You can only concentrate on a single
                            spell at a time.""").color(NamedTextColor.GOLD)
            )));
        }

        builder.append(Spellbook.LINE_BREAK.color(ACCENTS_STYLE))
                .append(spell.description().applyFallbackStyle(Spellbook.DESCRIPTION_COLOR));

        if (!collapse) {
            builder.append(attributeComponent);

            if (learningComponent != null) {
                builder.append(Component.newline()).append(learningComponent);
            }

            if (generationComponent != null) {
                builder.append(Component.newline()).append(generationComponent);
            }
        }

        return builder.toComponent();
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
                            method.describe(indent).color(NamedTextColor.GOLD)
                    );
                }).toList()
        );

        if (collapse) {
            return Component.text("[G]")
                    .color(CATEGORY_COLOR)
                    .hoverEvent(HoverEvent.showText(generation));
        } else {
            return Component.text("Generation: \n").color(CATEGORY_COLOR)
                    .append(generation);
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
                            return component.append(criteria.describe(indent).color(NamedTextColor.GOLD));
                        }).toList()
        );

        if (methodList.isEmpty()) {
            return null;
        }

        TextComponent text;
        if (collapse) {
            text = Component.text("[L]")
                    .color(CATEGORY_COLOR)
                    .hoverEvent(HoverEvent.showText(learning));
        } else {
            text = Component.text("Learning criteria: \n").color(CATEGORY_COLOR)
                    .append(learning);
        }
        return text;
    }

    private static @NonNull Component getAttributeComponent(SpellDefinition spell, boolean collapse) {
        Component attributes = Component.join(JoinConfiguration.newlines(), spell.getLore());

        if (collapse) {
            return Component.text("[A]")
                    .color(CATEGORY_COLOR)
                    .hoverEvent(HoverEvent.showText(attributes));
        } else {
            return Component.text("Attributes: \n")
                    .color(CATEGORY_COLOR)
                    .append(attributes);
        }
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        plugin.sendMessage("Usage: &h/" + context.getInput() + " <spell>", context.getSource().getSender());
        return Command.SINGLE_SUCCESS;
    }
}
