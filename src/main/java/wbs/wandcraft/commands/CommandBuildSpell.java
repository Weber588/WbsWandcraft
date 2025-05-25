package wbs.wandcraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument;
import wbs.utils.util.commands.brigadier.argument.WbsSimpleArgument.KeyedSimpleArgument;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WandcraftSettings;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;

import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class CommandBuildSpell extends WbsSubcommand {
    private static final KeyedSimpleArgument DEFINITION = new KeyedSimpleArgument(
            "definition",
            ArgumentTypes.namespacedKey(),
            null
    ).setKeyedSuggestions(WandcraftRegistries.SPELLS.values());
    public static final Material BASE_MATERIAL = Material.FLOW_BANNER_PATTERN;

    public CommandBuildSpell(@NotNull WbsPlugin plugin, @NotNull String label) {
        super(plugin, label);
        addSimpleArgument(DEFINITION);
    }

    @Override
    protected int onSimpleArgumentCallback(CommandContext<CommandSourceStack> context, WbsSimpleArgument.ConfiguredArgumentMap configuredArgumentMap) {
        NamespacedKey definitionKey = configuredArgumentMap.get(DEFINITION);

        if (definitionKey == null) {
            plugin.sendMessage("Choose a spell: "
                            + WandcraftRegistries.SPELLS.stream()
                            .map(Keyed::key)
                            .map(Key::asString)
                            .collect(Collectors.joining(", ")),
                    context.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        }

        SpellDefinition spell = WandcraftRegistries.SPELLS.get(definitionKey);

        if (spell == null) {
            plugin.sendMessage("Invalid spell definition: " + definitionKey.asString() + ".", context.getSource().getSender());
            return Command.SINGLE_SUCCESS;
        }

        ItemStack item = ItemStack.of(BASE_MATERIAL);
        SpellInstance spellInstance = new SpellInstance(spell);

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

            cloneBuilder.addString(spell.key().asString());
            item.setData(DataComponentTypes.CUSTOM_MODEL_DATA, cloneBuilder);

            item.setData(DataComponentTypes.ITEM_MODEL, BASE_MATERIAL.getKey());
            item.editMeta(meta -> {
                meta.setItemModel(BASE_MATERIAL.getKey());
            });
        } else {
            item.editMeta(meta -> {
                meta.setItemModel(
                    settings.getItemModel(
                            "spell_" + spell.getKey().asString(),
                            "spell_default"
                    )
                );
            });
        }

        spellInstance.toItem(item);

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
