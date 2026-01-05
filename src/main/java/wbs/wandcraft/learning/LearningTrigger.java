package wbs.wandcraft.learning;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class LearningTrigger<T extends Event> extends LearningMethod {
    public LearningTrigger(ConfigurationSection parentSection, String key, String directory) {
        super(parentSection, key, directory);
    }

    public abstract boolean matches(T t);
    public boolean shouldGrantOnLogin(Player player) {
        return false;
    }
    public abstract @Nullable Player getPlayer(T event);
    public abstract Class<T> getMatchClass();
}
