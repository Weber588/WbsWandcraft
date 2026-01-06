package wbs.wandcraft.learning;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.utils.exceptions.InvalidConfigurationException;
import wbs.wandcraft.WbsWandcraft;

@NullMarked
public class LearningMethodType<T extends Event> implements Keyed {
    public static <T extends Event> LearningMethodType<T> build(String nativeKey, LearningMethodProvider<T> provider) {
        return new LearningMethodType<>(WbsWandcraft.getKey(nativeKey), provider);
    }

    private final @NotNull NamespacedKey key;
    private final LearningMethodProvider<T> provider;

    public LearningMethodType(NamespacedKey key, LearningMethodProvider<T> provider) {
        this.key = key;
        this.provider = provider;
    }

    public LearningMethod construct(ConfigurationSection parentSection, String key, String directory) throws InvalidConfigurationException {
        return provider.construct(parentSection, key, directory);
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @FunctionalInterface
    public interface LearningMethodProvider<T extends Event> {
        LearningMethod construct(ConfigurationSection parentSection, String key, String directory) throws InvalidConfigurationException;
    }
}
