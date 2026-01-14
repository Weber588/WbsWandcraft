package wbs.wandcraft.learning;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.utils.util.WbsKeyed;

import java.util.List;

@NullMarked
public class DealDamageLearningTrigger extends LearningTrigger<EntityDamageByEntityEvent> {
    private final List<String> damageTypes;
    private final double minDamage;

    public DealDamageLearningTrigger(ConfigurationSection parentSection, String key, String directory) {
        super(parentSection, key, directory);

        if (parentSection.isList(key)) {
            minDamage = 0;
            damageTypes = parentSection.getStringList(key);
        } else {
            ConfigurationSection section = parentSection.getConfigurationSection(key);
            if (section == null) {
                throw new InvalidConfigurationException("Section or list required.", directory);
            }

            minDamage = section.getInt("min-damage");
            damageTypes = section.getStringList("damage-types");
        }

        if (damageTypes.isEmpty()) {
            throw new InvalidConfigurationException("Damage types list cannot be empty.", directory);
        }
    }

    public boolean matches(EntityDamageByEntityEvent event) {
        double damage = event.getDamage();

        if (minDamage > damage) {
            return false;
        }

        return damageTypes.stream().anyMatch(event.getDamageSource().getDamageType().key().asString()::matches);
    }

    @Override
    public @Nullable Player getPlayer(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            return player;
        }
        return null;
    }

    @Override
    public Class<EntityDamageByEntityEvent> getMatchClass() {
        return EntityDamageByEntityEvent.class;
    }

    private List<DamageType> getDamageTypes() {
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.DAMAGE_TYPE).stream()
                .filter(damageType -> damageTypes.stream().anyMatch(damageType.getKey().asString()::matches))
                .toList();
    }

    @Override
    public @NotNull Component describe(Component indent, boolean shorten) {
        Component message = Component.text("Deal ");

        if (minDamage > 0) {
            message = message.append(Component.text("at least " + minDamage + " "));
        }

        List<DamageType> damageTypes = getDamageTypes();

        message = message.append(Component.text("damage of one of these types: "));
        Component joiner = Component.newline().append(indent).append(Component.text(" - "));
        if (damageTypes.size() > 5 && shorten) {
            Component hover = Component.text("Hover").color(TextColor.color(NamedTextColor.AQUA));

            Component hoverText = Component.join(
                    JoinConfiguration.builder()
                            .separator(joiner)
                            .build(),
                    damageTypes.stream()
                            .map(damageType -> Component.text(WbsKeyed.toPrettyString(damageType)).color(NamedTextColor.AQUA))
                            .toList()
            );

            hover = hover.hoverEvent(HoverEvent.showText(hoverText));
            return message.append(hover);
        } else {
            for (DamageType damageType : damageTypes) {
                message = message
                        .append(joiner)
                        .append(Component.text(WbsKeyed.toPrettyString(damageType)).color(NamedTextColor.AQUA));
            }

            return message;
        }
    }
}
