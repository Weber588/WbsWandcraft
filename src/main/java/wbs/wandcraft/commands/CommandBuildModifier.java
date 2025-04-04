package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsEnumArgumentType;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.modifier.ModifierScope;
import wbs.wandcraft.spell.modifier.SpellModifier;

@SuppressWarnings("UnstableApiUsage")
public class CommandBuildModifier extends WbsSubcommand {
    private static final WbsSimpleArgument<ModifierScope> SCOPE = new WbsSimpleArgument<>(
            "modifier_scope",
            new WbsEnumArgumentType<>(ModifierScope.class),
            ModifierScope.NEXT,
            ModifierScope.class
    ).addSuggestions(ModifierScope.values());

    public CommandBuildModifier(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
        addSimpleArgument(SCOPE);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        ModifierScope scope = configuredArgumentMap.get(SCOPE);

        ItemStack item = ItemStack.of(Material.GLOBE_BANNER_PATTERN);
        SpellModifier modifier = new SpellModifier(scope);

        item.getDataTypes().forEach(item::unsetData);

        item.editMeta(meta -> {
            meta.setItemModel(WbsWandcraft.getInstance().getSettings().getItemModel("modifier"));
        });

        modifier.toItem(item);

        if (context.getSource().getSender() instanceof Player player) {
            player.getInventory().addItem(item);
        }

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> commandContext) {
        return 0;
    }
}
