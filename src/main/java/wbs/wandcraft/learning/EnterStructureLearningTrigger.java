package wbs.wandcraft.learning;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.generator.structure.GeneratedStructure;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.utils.exceptions.InvalidConfigurationException;

import java.util.List;
import java.util.stream.Collectors;

@NullMarked
public class EnterStructureLearningTrigger extends LearningTrigger<PlayerMoveEvent> {
    private final List<String> structureRegex;

    public EnterStructureLearningTrigger(ConfigurationSection parentSection, String key, String directory) {
        super(parentSection, key, directory);

        structureRegex = parentSection.getStringList(key);

        if (structureRegex.isEmpty()) {
            throw new InvalidConfigurationException("Structure regex list cannot be empty.", directory);
        }
    }

    public boolean matches(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();

        List<GeneratedStructure> oldStructures = getStructuresAt(from);
        List<GeneratedStructure> newStructures = getStructuresAt(to);

        newStructures.removeIf(check ->
                oldStructures.stream().anyMatch(check2 ->
                        check2.getStructure().getStructureType().key().equals(check.getStructure().getStructureType().key())
                )
        );

        if (newStructures.isEmpty()) {
            return false;
        }

        return newStructures.stream().anyMatch(structure -> {
            Key structureKey = RegistryAccess.registryAccess().getRegistry(RegistryKey.STRUCTURE).getKey(structure.getStructure());
            if (structureKey == null) {
                return false;
            }
            String keyString = structureKey.asString();
            return structureRegex.stream().anyMatch(keyString::matches);
        });
    }

    @Override
    public Class<PlayerMoveEvent> getMatchClass() {
        return PlayerMoveEvent.class;
    }

    public List<GeneratedStructure> getStructuresAt(Location location) {
        return location.getChunk().getStructures().stream()
                .filter(structure ->
                        structure.getPieces().stream()
                                .anyMatch(piece ->
                                        piece.getBoundingBox().contains(location.toVector())
                                )
                ).collect(Collectors.toList());
    }

    @Override
    public Player getPlayer(PlayerMoveEvent event) {
        return event.getPlayer();
    }

    @Override
    public @NotNull Component describe(Component indent, boolean shorten) {
        Component message = Component.text("Enter one of these structures: ");

        List<NamespacedKey> structureKeys = getStructureKeys();
        Component joiner = Component.newline().append(indent).append(Component.text(" - "));
        if (structureKeys.size() > 5 && shorten) {
            Component hover = Component.text("Hover").color(TextColor.color(NamedTextColor.AQUA));

            Component hoverText = Component.join(
                    JoinConfiguration.builder()
                            .separator(joiner)
                            .build(),
                    structureKeys.stream()
                            .map(structureKey -> Component.text(structureKey.asString()).color(NamedTextColor.AQUA))
                            .toList()
            );

            hover = hover.hoverEvent(HoverEvent.showText(hoverText));
            return message.append(hover);
        } else {
            for (NamespacedKey structureKey : structureKeys) {
                message = message.append(joiner).append(Component.text(structureKey.asString()).color(NamedTextColor.AQUA));
            }

            return message;
        }
    }

    private List<NamespacedKey> getStructureKeys() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.STRUCTURE).keyStream()
                .filter(structureKey -> structureRegex.stream().anyMatch(structureKey.asString()::matches))
                .toList();
    }
}
