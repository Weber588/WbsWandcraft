package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.commands.brigadier.argument.WbsRegistrySimpleArgument;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsMessageBuilder;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.MagicDomain;
import wbs.wandcraft.spell.dynamic.FixedDomainSpellArchetype;
import wbs.wandcraft.spell.dynamic.SpellArchetype;
import wbs.wandcraft.util.MenuUtils;

public class CommandInfoSpellArchetype extends CommandInfo<SpellArchetype> {
    private static final WbsRegistrySimpleArgument<SpellArchetype> SPELL_ARCHETYPE = new WbsRegistrySimpleArgument<>(
            "spell_archetype",
            WbsWandcraft.getInstance(),
            "spell archetype",
            SpellArchetype.class,
            WandcraftRegistries.SPELL_ARCHETYPES
    ).isRequired(true);

    public static Component getSpellArchetypePage(SpellArchetype archetype) {
        WbsMessageBuilder builder = WbsWandcraft.getInstance().buildMessageNoPrefix(archetype.displayName());

        if (archetype instanceof FixedDomainSpellArchetype fixedArchetype) {
            MagicDomain domain = fixedArchetype.primaryDomain();
            builder.append(Component.newline()).append(domain.displayName()
                    .hoverEvent(HoverEvent.showText(domain.getAttributesText())));
        }

        builder.append(MenuUtils.LINE_BREAK)
                .append(archetype.description().applyFallbackStyle(MenuUtils.DESCRIPTION_STYLE));

        /*
        builder = builder.onClick(ClickEvent.callback(audience -> {
            readImage(WbsStrings.capitalize(type.getKey().value()) + ".png", (Player) audience, 0.1, 1, 1);
        }, ClickCallback.Options.builder().uses(Integer.MAX_VALUE).build()));
*/
        return builder.toComponent();
    }

    public CommandInfoSpellArchetype(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label, WandcraftRegistries.SPELL_ARCHETYPES);
        
        this.addSimpleArgument(SPELL_ARCHETYPE);
    }

    @Override
    protected Component getComponent(@UnknownNullability SpellArchetype archetype, boolean collapse) {
        return getSpellArchetypePage(archetype);
    }

    @Override
    protected @Nullable SpellArchetype getT(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        return SPELL_ARCHETYPE.getRequiredValue(context);
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        plugin.sendMessage("Usage: &h/" + context.getInput() + " <spell>", context.getSource().getSender());
        return Command.SINGLE_SUCCESS;
    }
}
