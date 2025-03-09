package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.ItemDecorator;
import wbs.wandcraft.util.CustomPersistentDataTypes;
import wbs.wandcraft.wand.Wand;
import wbs.wandcraft.wand.WandInventoryType;

@SuppressWarnings("UnstableApiUsage")
public class CommandBuildWand extends WbsSubcommand {
    private static final WbsSimpleArgument<Integer> COOLDOWN = Wand.WAND_COOLDOWN.getArg();
    private static final WbsSimpleArgument<Integer> DELAY = Wand.WAND_CAST_DELAY.getArg();

    public CommandBuildWand(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
        addSimpleArgument(COOLDOWN);
        addSimpleArgument(DELAY);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        int cooldown = configuredArgumentMap.get(COOLDOWN);
        int delay = configuredArgumentMap.get(DELAY);

        ItemStack wandItem = ItemStack.of(Material.STICK);
        Wand wand = new Wand(WandInventoryType.PLANE_3x3);

        wand.setAttribute(Wand.WAND_COOLDOWN, cooldown);
        wand.setAttribute(Wand.WAND_CAST_DELAY, delay);

        wandItem.editMeta(meta -> {
            for (ItemFlag value : ItemFlag.values()) {
                meta.addItemFlags(value);
            }

            meta.getPersistentDataContainer().set(Wand.WAND_KEY, CustomPersistentDataTypes.WAND, wand);
            ItemDecorator.decorate(wand, meta);
        });

        if (context.getSource().getSender() instanceof Player player) {
            player.getInventory().addItem(wandItem);
        }

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> commandContext) {
        return 0;
    }
}
