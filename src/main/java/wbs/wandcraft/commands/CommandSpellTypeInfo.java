package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.commands.brigadier.argument.WbsRegistrySimpleArgument;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.SpellType;
import wbs.wandcraft.util.MenuUtils;

public class CommandSpellTypeInfo extends CommandInfo<SpellType> {
    private static final WbsRegistrySimpleArgument<SpellType> SPELL_TYPE = new WbsRegistrySimpleArgument<>(
            "spell_type",
            WbsWandcraft.getInstance(),
            "spell type",
            SpellType.class,
            WandcraftRegistries.SPELL_TYPES
    ).isRequired(true);

    public CommandSpellTypeInfo(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label, WandcraftRegistries.SPELL_TYPES);
        
        this.addSimpleArgument(SPELL_TYPE);
    }

    @Override
    protected Component getComponent(@UnknownNullability SpellType type, boolean collapse) {
        Component attributeComponent = getAttributeComponent(type, collapse);

        WbsMessageBuilder builder = plugin.buildMessageNoPrefix(type.displayName());

        if (collapse) {
            builder.append(" ").append(attributeComponent);
        }

        builder.append(MenuUtils.LINE_BREAK)
                .append(type.description().applyFallbackStyle(MenuUtils.DESCRIPTION_COLOR));

        if (!collapse) {
            builder.append(attributeComponent);
        }

        return builder.toComponent();
    }

    @Override
    protected @Nullable SpellType getT(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        return SPELL_TYPE.getRequiredValue(context);
    }

    private static @NonNull Component getAttributeComponent(SpellType type, boolean collapse) {
        Component attributes = type.getAttributesText();
        TextComponent descriptionText = Component.text("Attributes: \n")
                .color(MenuUtils.EXTRAS_COLOUR)
                .append(attributes);

        if (collapse) {
            return Component.text("[A]")
                    .color(MenuUtils.EXTRAS_COLOUR)
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
