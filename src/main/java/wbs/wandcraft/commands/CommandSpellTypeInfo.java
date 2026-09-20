package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsRegistrySimpleArgument;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.SpellType;

public class CommandSpellTypeInfo extends WbsSubcommand {
    private static final WbsRegistrySimpleArgument<SpellType> SPELL_TYPE = new WbsRegistrySimpleArgument<>(
            "spell_type",
            WbsWandcraft.getInstance(),
            "spell type",
            SpellType.class,
            WandcraftRegistries.SPELL_TYPES
    ).isRequired(true);

    public CommandSpellTypeInfo(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
        
        this.addSimpleArgument(SPELL_TYPE);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        SpellType type = SPELL_TYPE.getRequiredValue(context);

        if (type == null) {
            return Command.SINGLE_SUCCESS;
        }

        WbsMessageBuilder builder = plugin.buildMessageNoPrefix("=======================")
                .append("\nSpell Type: ")
                .append(type.displayName())
                .append("\nDescription: ")
                .append(type.description().color(NamedTextColor.GOLD))
                .append("\nFeatures: ");

        Component attributeEffects = Component.text("\n[Attributes]").color(NamedTextColor.AQUA);

        builder.append(attributeEffects.hoverEvent(HoverEvent.showText(type.getAttributesText())));

        // TODO: Show triggered events? Sorted by spell aspect?

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
