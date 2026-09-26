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
    private static final WbsRegistrySimpleArgument<SpellType> PRIMARY_SPELL_TYPE = new WbsRegistrySimpleArgument<>(
            "spell_type_primary",
            WbsWandcraft.getInstance(),
            "spell type",
            SpellType.class,
            WandcraftRegistries.SPELL_TYPES
    ).isRequired(true);
    private static final WbsRegistrySimpleArgument<SpellType> SECONDARY_SPELL_TYPE = new WbsRegistrySimpleArgument<>(
            "spell_type_secondary",
            WbsWandcraft.getInstance(),
            "spell type",
            SpellType.class,
            WandcraftRegistries.SPELL_TYPES
    );

    public CommandSpellCastDynamic(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);

        this.addSimpleArgument(ASPECT);
        this.addSimpleArgument(PRIMARY_SPELL_TYPE);
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

        SpellType primarySpellType = PRIMARY_SPELL_TYPE.getRequiredValue(context);
        if (primarySpellType == null) {
            return Command.SINGLE_SUCCESS;
        }

        SpellType secondarySpellType = configuredArgumentMap.get(SECONDARY_SPELL_TYPE);

        DynamicSpell built = aspect.build(primarySpellType, secondarySpellType);

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
