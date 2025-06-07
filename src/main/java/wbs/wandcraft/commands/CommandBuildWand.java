package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsEnumArgumentType;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.util.ItemUtils;
import wbs.wandcraft.wand.WandInventoryType;
import wbs.wandcraft.wand.WandTexture;

import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class CommandBuildWand extends WbsSubcommand {
    private static final WbsSimpleArgument.KeyedSimpleArgument INVENTORY_TYPE = new WbsSimpleArgument.KeyedSimpleArgument(
            "inventory_type",
            ArgumentTypes.namespacedKey(),
            WandInventoryType.PLANE_3x3.getKey()
    ).addKeyedSuggestions(WandcraftRegistries.WAND_INVENTORY_TYPES.values());
    private static final WbsSimpleArgument<ItemUseAnimation> ANIMATION_TYPE = new WbsSimpleArgument<>(
            "animation_type",
            new WbsEnumArgumentType<>(ItemUseAnimation.class),
            null,
            ItemUseAnimation.class
    ).addSuggestions(ItemUseAnimation.values());
    private static final WbsSimpleArgument<Float> ANIMATION_DURATION = new WbsSimpleArgument<>(
            "animation_duration",
            FloatArgumentType.floatArg(0),
            1f,
            Float.class
    ).addSuggestions(0f, 0.25f, 0.5f, 0.75f, 1f);
    private static final WbsSimpleArgument.KeyedSimpleArgument WAND_TEXTURE = new WbsSimpleArgument.KeyedSimpleArgument(
            "wand_texture",
            ArgumentTypes.namespacedKey(),
            WandTexture.GEM.getKey()
    ).addKeyedSuggestions(WandcraftRegistries.WAND_TEXTURES.values());
    private static final WbsSimpleArgument<Double> WAND_HUE = new WbsSimpleArgument<>(
            "wand_hue",
            DoubleArgumentType.doubleArg(-1f, 1f),
            -1d,
            Double.class
    ).addSuggestions(0d, 0.25d, 0.5d, 0.75d, 1d);

    public CommandBuildWand(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
        addSimpleArgument(INVENTORY_TYPE);
        addSimpleArgument(ANIMATION_TYPE);
        addSimpleArgument(ANIMATION_DURATION);
        addSimpleArgument(WAND_TEXTURE);
        addSimpleArgument(WAND_HUE);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        NamespacedKey inventoryTypeKey = configuredArgumentMap.get(INVENTORY_TYPE);

        if (inventoryTypeKey == null) {
            plugin.sendMessage("Choose an inventory type: "
                            + WandcraftRegistries.WAND_INVENTORY_TYPES.stream()
                            .map(Keyed::key)
                            .map(Key::asString)
                            .collect(Collectors.joining(", ")),
                    context.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        }

        WandInventoryType inventoryType = WandcraftRegistries.WAND_INVENTORY_TYPES.get(inventoryTypeKey);

        if (inventoryType == null) {
            plugin.sendMessage("Invalid inventory type key: " + inventoryTypeKey.asString() + ".", context.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        }

        WandTexture wandTexture = WandcraftRegistries.WAND_TEXTURES.get(configuredArgumentMap.get(WAND_TEXTURE));
        Double hue = configuredArgumentMap.get(WAND_HUE);
        ItemUseAnimation animation = configuredArgumentMap.get(ANIMATION_TYPE);
        Float animationSeconds = configuredArgumentMap.get(ANIMATION_DURATION);

        ItemStack wandItem = ItemUtils.buildWand(
                inventoryType,
                wandTexture,
                hue,
                animation,
                animationSeconds
        );

        if (context.getSource().getSender() instanceof Player player) {
            player.getInventory().addItem(wandItem);
        }

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        return sendSimpleArgumentUsage(context);
    }
}
