package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.UseCooldown;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsEnumArgumentType;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.wand.Wand;
import wbs.wandcraft.wand.WandInventoryType;

import java.util.UUID;
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

    public CommandBuildWand(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
        addSimpleArgument(INVENTORY_TYPE);
        addSimpleArgument(ANIMATION_TYPE);
        addSimpleArgument(ANIMATION_DURATION);
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

        ItemStack item = ItemStack.of(Material.STICK);
        Wand wand = new Wand(inventoryType);

        item.getDataTypes().forEach(item::unsetData);
        item.setData(DataComponentTypes.ITEM_NAME, Component.text("Wand"));
        item.setData(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(0.0001f)
                .cooldownGroup(WbsWandcraft.getKey(UUID.randomUUID().toString()))
        );

        ItemUseAnimation animation = configuredArgumentMap.get(ANIMATION_TYPE);

        if (animation != null) {
            float animationSeconds = configuredArgumentMap.get(ANIMATION_DURATION);

            item.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                    .animation(animation)
                    .hasConsumeParticles(false)
                    .consumeSeconds(animationSeconds)
                    .sound(Key.key("invalid:invalid"))
            );
        }

        item.editMeta(meta -> {
            meta.setItemModel(WbsWandcraft.getInstance().getSettings().getItemModel("wand"));
        });

        wand.toItem(item);

        if (context.getSource().getSender() instanceof Player player) {
            player.getInventory().addItem(item);
        }

        return Command.SINGLE_SUCCESS;
    }

    @Override
    protected int executeNoArgs(CommandContext<CommandSourceStack> context) {
        return sendSimpleArgumentUsage(context);
    }
}
