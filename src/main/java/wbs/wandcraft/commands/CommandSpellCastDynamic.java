package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Style;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsRegistrySimpleArgument;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastingQueue;
import wbs.wandcraft.spell.SpellType;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.dynamic.DynamicSpell;
import wbs.wandcraft.spell.dynamic.FixedTypeSpellAspect;
import wbs.wandcraft.spell.dynamic.GenericSpellAspect;
import wbs.wandcraft.spell.dynamic.SpellAspect;
import wbs.wandcraft.util.MenuUtils;

import java.util.Set;

public class CommandSpellCastDynamic extends WbsSubcommand {
    private static final WbsRegistrySimpleArgument<SpellAspect> ASPECT = new WbsRegistrySimpleArgument<>(
            "aspect",
            WbsWandcraft.getInstance(),
            "spell aspect",
            SpellAspect.class,
            WandcraftRegistries.SPELL_ASPECTS
    ).isRequired(true);
    private static final WbsRegistrySimpleArgument<SpellType> SPELL_TYPE = new WbsRegistrySimpleArgument<>(
            "spell_type",
            WbsWandcraft.getInstance(),
            "spell type",
            SpellType.class,
            WandcraftRegistries.SPELL_TYPES
    );
    private static final WbsRegistrySimpleArgument<SpellType> SECONDARY_SPELL_TYPE = new WbsRegistrySimpleArgument<>(
            "spell_type_secondary",
            WbsWandcraft.getInstance(),
            "spell type",
            SpellType.class,
            WandcraftRegistries.SPELL_TYPES
    );

    static {
        SECONDARY_SPELL_TYPE.setSuggestionProvider((context, builder) -> {
            SpellAspect value = ASPECT.getValue(context);

            // Don't suggest a 2nd type if a fixed aspect already has a first
            if (value instanceof FixedTypeSpellAspect) {
                return builder.buildFuture();
            }

            return SECONDARY_SPELL_TYPE.getSuggestions(context, builder);
        });
    }

    public CommandSpellCastDynamic(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);

        this.addSimpleArgument(ASPECT);
        this.addSimpleArgument(SPELL_TYPE);
        this.addSimpleArgument(SECONDARY_SPELL_TYPE);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        CommandSender sender = context.getSource().getSender();
        if (!(sender instanceof Player player)) {
            plugin.sendMessage("This command is only usable by players.", sender);
            return Command.SINGLE_SUCCESS;
        }

        SpellAspect aspect = ASPECT.getRequiredValue(context);

        if (aspect == null) {
            return Command.SINGLE_SUCCESS;
        }

        DynamicSpell built;
        if (aspect instanceof GenericSpellAspect genericAspect) {
            SpellType primarySpellType = SPELL_TYPE.getRequiredValue(context);
            if (primarySpellType == null) {
                return Command.SINGLE_SUCCESS;
            }

            SpellType secondarySpellType = configuredArgumentMap.get(SECONDARY_SPELL_TYPE);

            built = genericAspect.build(primarySpellType, secondarySpellType);
        } else if (aspect instanceof FixedTypeSpellAspect fixedAspect) {
            SpellType primarySpellType = configuredArgumentMap.get(SPELL_TYPE);

            built = fixedAspect.build(primarySpellType);
        } else {
            throw new IllegalStateException("Unknown spell aspect!");
        }

        SpellInstance instance = built.newInstance();

        Component attributesHover = Component.join(
                JoinConfiguration.newlines(),
                instance.deriveAttributeValues().stream()
                        .sorted()
                        .map(attr ->
                                (Component) Component.text("  - ")
                                        .style(Style.style(MenuUtils.ACCENTS_STYLE, Set.of()))
                                        .append(attr.toComponent().color(MenuUtils.EXTRAS_COLOUR))
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
