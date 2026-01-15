package wbs.wandcraft.learning;

import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.configuration.WbsConfigReader;
import wbs.wandcraft.WbsWandcraft;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@NullMarked
public class PlaceBlockLearningTrigger extends LearningTrigger<BlockBreakEvent> {
    private final List<BlockType> blockTypes = new LinkedList<>();
    private final int totalRequired;

    public PlaceBlockLearningTrigger(ConfigurationSection parentSection, String key, String directory) {
        super(parentSection, key, directory);

        if (parentSection.isList(key)) {
            totalRequired = 0;
            getBlockTypes(parentSection, key, directory);
        } else {
            ConfigurationSection section = parentSection.getConfigurationSection(key);
            if (section == null) {
                throw new InvalidConfigurationException("Section or list required.", directory);
            }

            totalRequired = section.getInt("total-required");
            getBlockTypes(section, "block-types", directory);
        }

        if (blockTypes.isEmpty()) {
            throw new InvalidConfigurationException("Entity types list cannot be empty.", directory);
        }
    }

    private void getBlockTypes(ConfigurationSection section, String key, String directory) {
        blockTypes.addAll(WbsConfigReader.getRegistryEntries(section, key, RegistryKey.BLOCK, WbsWandcraft.getInstance().getSettings(), directory));
    }

    public boolean matches(BlockBreakEvent event) {
        Player player = event.getPlayer();

        Block block = event.getBlock();

        if (!blockTypes.contains(block.getType().asBlockType())) {
            return false;
        }

        if (totalRequired > 1) {
            // Add 1 for this kill
            int total = getNetPlaced(player) + 1;
            if (total < totalRequired) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean shouldGrantOnLogin(Player player) {
        return getNetPlaced(player) >= totalRequired;
    }

    private int getNetPlaced(Player player) {
        int total = 0;
        for (BlockType type : blockTypes) {
            //noinspection deprecation
            Material material = Objects.requireNonNull(type.asMaterial());

            total += player.getStatistic(Statistic.USE_ITEM, material);
            total -= player.getStatistic(Statistic.MINE_BLOCK, material);
        }
        return total;
    }

    @Override
    public Class<BlockBreakEvent> getMatchClass() {
        return BlockBreakEvent.class;
    }

    @Override
    public @Nullable Player getPlayer(BlockBreakEvent event) {
        return event.getPlayer();
    }

    @Override
    public @NotNull Component describe(Component indent, boolean shorten) {
        Component message = Component.text("Kill ");

        if (totalRequired > 0) {
            message = message.append(Component.text("at least " + totalRequired + " "));
        }

        message = message.append(Component.text(" mobs of type: "));
        Component joiner = Component.newline().append(indent).append(Component.text(" - "));
        if (blockTypes.size() > 5 && shorten) {
            Component hover = Component.text("Hover").color(TextColor.color(NamedTextColor.AQUA));

            Component hoverText = Component.join(
                    JoinConfiguration.builder()
                            .separator(joiner)
                            .build(),
                    blockTypes.stream()
                            .map(damageType -> Component.translatable(damageType.translationKey()).color(NamedTextColor.AQUA))
                            .toList()
            );

            hover = hover.hoverEvent(HoverEvent.showText(hoverText));
            return message.append(hover);
        } else {
            for (BlockType type : blockTypes) {
                message = message
                        .append(joiner)
                        .append(Component.translatable(type.translationKey()).color(NamedTextColor.AQUA));
            }

            return message;
        }
    }
}
