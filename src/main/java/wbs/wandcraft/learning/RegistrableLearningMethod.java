package wbs.wandcraft.learning;

import org.bukkit.configuration.ConfigurationSection;

public abstract class RegistrableLearningMethod extends LearningMethod {
    public RegistrableLearningMethod(ConfigurationSection parentSection, String key, String directory) {
        super(parentSection, key, directory);
    }

    public abstract void registerEvents();
}
