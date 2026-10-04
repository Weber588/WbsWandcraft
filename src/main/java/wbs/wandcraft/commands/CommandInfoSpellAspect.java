package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.commands.brigadier.argument.WbsRegistrySimpleArgument;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.dynamic.FixedTypeSpellAspect;
import wbs.wandcraft.spell.dynamic.SpellAspect;
import wbs.wandcraft.util.MenuUtils;

public class CommandInfoSpellAspect extends CommandInfo<SpellAspect> {
    private static final WbsRegistrySimpleArgument<SpellAspect> SPELL_ASPECT = new WbsRegistrySimpleArgument<>(
            "spell_aspect",
            WbsWandcraft.getInstance(),
            "spell aspect",
            SpellAspect.class,
            WandcraftRegistries.SPELL_ASPECTS
    ).isRequired(true);

    public static Component getSpellAspectPage(SpellAspect aspect) {
        WbsMessageBuilder builder = WbsWandcraft.getInstance().buildMessageNoPrefix(aspect.displayName());

        if (aspect instanceof FixedTypeSpellAspect fixedAspect) {
            builder.append(Component.newline()).append(fixedAspect.primaryType().displayName());
        }

        builder.append(MenuUtils.LINE_BREAK)
                .append(aspect.description().applyFallbackStyle(MenuUtils.DESCRIPTION_COLOR));

        /*
        builder = builder.onClick(ClickEvent.callback(audience -> {
            readImage(WbsStrings.capitalize(type.getKey().value()) + ".png", (Player) audience, 0.1, 1, 1);
        }, ClickCallback.Options.builder().uses(Integer.MAX_VALUE).build()));
*/
        return builder.toComponent();
    }

    public CommandInfoSpellAspect(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label, WandcraftRegistries.SPELL_ASPECTS);
        
        this.addSimpleArgument(SPELL_ASPECT);
    }

    @Override
    protected Component getComponent(@UnknownNullability SpellAspect aspect, boolean collapse) {
        return getSpellAspectPage(aspect);
    }

    @Override
    protected @Nullable SpellAspect getT(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        return SPELL_ASPECT.getRequiredValue(context);
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        plugin.sendMessage("Usage: &h/" + context.getInput() + " <spell>", context.getSource().getSender());
        return Command.SINGLE_SUCCESS;
    }
}
