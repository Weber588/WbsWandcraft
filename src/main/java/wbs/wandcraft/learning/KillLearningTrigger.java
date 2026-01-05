package wbs.wandcraft.learning;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Statistic;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsEnums;
import wbs.wandcraft.WbsWandcraft;

import java.util.LinkedList;
import java.util.List;

@NullMarked
public class KillLearningTrigger extends LearningTrigger<EntityDeathEvent> {
    private final List<EntityType> entityTypes = new LinkedList<>();
    private final int killsRequired;

    public KillLearningTrigger(ConfigurationSection parentSection, String key, String directory) {
        super(parentSection, key, directory);

        if (parentSection.isList(key)) {
            killsRequired = 0;
            List<String> entityTypeStrings = parentSection.getStringList(key);

            getEntityTypes(entityTypeStrings, directory);
        } else {
            ConfigurationSection section = parentSection.getConfigurationSection(key);
            if (section == null) {
                throw new InvalidConfigurationException("Section or list required.", directory);
            }

            killsRequired = section.getInt("kills-required");
            getEntityTypes(section.getStringList("entity-types"), directory);
        }

        if (entityTypes.isEmpty()) {
            throw new InvalidConfigurationException("Entity types list cannot be empty.", directory);
        }
    }

    private void getEntityTypes(List<String> entityTypeStrings, String directory) {
        for (String typeString : entityTypeStrings) {
            EntityType type = WbsEnums.getEnumFromString(EntityType.class, typeString);
            if (type == null) {
                WbsWandcraft.getInstance().getSettings().logError("Invalid type: " + typeString, directory);
            } else {
                entityTypes.add(type);
            }
        }
    }

    public boolean matches(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player player = entity.getKiller();

        if (player == null) {
            return false;
        }

        if (!entityTypes.contains(entity.getType())) {
            return false;
        }

        if (killsRequired > 1) {
            // Add 1 for this kill
            int total = getKilledMatchingTypes(player) + 1;
            if (total < killsRequired) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean shouldGrantOnLogin(Player player) {
        return getKilledMatchingTypes(player) >= killsRequired;
    }

    private int getKilledMatchingTypes(Player player) {
        int total = 0;
        for (EntityType entityType : entityTypes) {
            total += player.getStatistic(Statistic.KILL_ENTITY, entityType);
        }
        return total;
    }

    @Override
    public Class<EntityDeathEvent> getMatchClass() {
        return EntityDeathEvent.class;
    }

    @Override
    public @Nullable Player getPlayer(EntityDeathEvent event) {
        return event.getEntity().getKiller();
    }

    @Override
    public @NotNull Component describe(Component indent, boolean shorten) {
        Component message = Component.text("Kill ");

        if (killsRequired > 0) {
            message = message.append(Component.text("at least " + killsRequired + " "));
        }

        message = message.append(Component.text(" mobs of type: "));
        Component joiner = Component.newline().append(indent).append(Component.text(" - "));
        if (entityTypes.size() > 5 && shorten) {
            Component hover = Component.text("Hover").color(TextColor.color(NamedTextColor.AQUA));

            Component hoverText = Component.join(
                    JoinConfiguration.builder()
                            .separator(joiner)
                            .build(),
                    entityTypes.stream()
                            .map(damageType -> Component.translatable(damageType.translationKey()).color(NamedTextColor.AQUA))
                            .toList()
            );

            hover = hover.hoverEvent(HoverEvent.showText(hoverText));
            return message.append(hover);
        } else {
            for (EntityType type : entityTypes) {
                message = message
                        .append(joiner)
                        .append(Component.translatable(type.translationKey()).color(NamedTextColor.AQUA));
            }

            return message;
        }
    }
}
