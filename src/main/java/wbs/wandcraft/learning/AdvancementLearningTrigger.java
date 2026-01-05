package wbs.wandcraft.learning;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.utils.exceptions.InvalidConfigurationException;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@NullMarked
public class AdvancementLearningTrigger extends LearningTrigger<PlayerAdvancementDoneEvent> {
    private final List<String> advancementRegex;

    public AdvancementLearningTrigger(ConfigurationSection parentSection, String key, String directory) {
        super(parentSection, key, directory);

        advancementRegex = parentSection.getStringList(key);

        if (advancementRegex.isEmpty()) {
            throw new InvalidConfigurationException("Advancement regex list cannot be empty.", directory);
        }
    }

    public boolean matches(PlayerAdvancementDoneEvent event) {
        Advancement advancement = event.getAdvancement();
        return matches(advancement);
    }

    @Override
    public boolean shouldGrantOnLogin(Player player) {
        List<Advancement> advancements = getAdvancements();
        for (Advancement check : advancements) {
            AdvancementProgress progress = player.getAdvancementProgress(check);

            if (progress.isDone()) {
                return true;
            }
        }

        return false;
    }

    private boolean matches(Advancement advancement) {
        return advancementRegex.stream().anyMatch(advancement.key().asString()::matches);
    }

    @Override
    public Player getPlayer(PlayerAdvancementDoneEvent event) {
        return event.getPlayer();
    }

    @Override
    public Class<PlayerAdvancementDoneEvent> getMatchClass() {
        return PlayerAdvancementDoneEvent.class;
    }

    @Override
    public @NotNull Component describe(Component indent, boolean shorten) {
        List<Advancement> advancements = getAdvancements();

        Component message = Component.text("Gain one of these advancements: ");
        Component joiner = Component.newline().append(indent).append(Component.text(" - "));
        if (advancements.size() > 5 && shorten) {
            Component hover = Component.text("Hover").color(TextColor.color(NamedTextColor.AQUA));

            Component hoverText = Component.join(
                    JoinConfiguration.builder()
                            .separator(joiner)
                            .build(),
                    advancements.stream()
                            .map(advancement -> advancement.displayName().applyFallbackStyle(NamedTextColor.AQUA))
                            .toList()
            );

            hover = hover.hoverEvent(HoverEvent.showText(hoverText));
            return message.append(hover);
        } else {
            for (Advancement advancement : advancements) {
                message = message.append(joiner)
                        .append(advancement.displayName().applyFallbackStyle(NamedTextColor.AQUA));
            }

            return message;
        }
    }

    private @NotNull List<Advancement> getAdvancements() {
        List<Advancement> advancements = new LinkedList<>();
        for (@NotNull Iterator<Advancement> it = Bukkit.advancementIterator(); it.hasNext(); ) {
            Advancement check = it.next();

            if (!matches(check)) {
                continue;
            }

            advancements.add(check);
        }
        return advancements;
    }
}
