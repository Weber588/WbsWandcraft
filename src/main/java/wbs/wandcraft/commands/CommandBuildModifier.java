package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsEnumArgumentType;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftSettings;
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
    public static final Material BASE_MATERIAL = Material.GLOBE_BANNER_PATTERN;

    public CommandBuildModifier(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
        addSimpleArgument(SCOPE);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        ModifierScope scope = configuredArgumentMap.get(SCOPE);

        ItemStack item = ItemStack.of(BASE_MATERIAL);
        SpellModifier modifier = new SpellModifier(scope);

        item.getDataTypes().forEach(item::unsetData);
        WandcraftSettings settings = WbsWandcraft.getInstance().getSettings();
        if (settings.useResourcePack()) {
            CustomModelData data = item.getData(DataComponentTypes.CUSTOM_MODEL_DATA);

            CustomModelData.Builder cloneBuilder = CustomModelData.customModelData();
            if (data != null) {
                cloneBuilder.addColors(data.colors());
                cloneBuilder.addFlags(data.flags());
                cloneBuilder.addFloats(data.floats());
                cloneBuilder.addStrings(data.strings());
            }

            cloneBuilder.addString(scope.key().asString());
            item.setData(DataComponentTypes.CUSTOM_MODEL_DATA, cloneBuilder);

            item.setData(DataComponentTypes.ITEM_MODEL, BASE_MATERIAL.getKey());
            item.editMeta(meta -> {
                meta.setItemModel(BASE_MATERIAL.getKey());
            });
        } else {
            item.editMeta(meta -> {
                meta.setItemModel(
                        settings.getItemModel(
                                "modifier_" + scope.name().toLowerCase(),
                                "modifier_default"
                        )
                );
            });
        }

        modifier.toItem(item);

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
