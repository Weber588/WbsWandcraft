package wbs.wandcraft.learning;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;

public abstract class LearningMethod {
    public LearningMethod(ConfigurationSection parentSection, String key, String directory) {

    }

    public @NotNull Component describe(Component indent) {
        return describe(indent, true);
    }
    public abstract @NotNull Component describe(Component indent, boolean shorten);
}
