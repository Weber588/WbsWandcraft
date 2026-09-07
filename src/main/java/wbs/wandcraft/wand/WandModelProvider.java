package wbs.wandcraft.wand;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.resourcepack.ItemModelProvider;

@NullMarked
public abstract class WandModelProvider implements Keyed, ItemModelProvider {
    protected final char backgroundChar;
    private final NamespacedKey key;

    protected WandModelProvider(NamespacedKey key, char backgroundChar) {
        this.key = key;
        this.backgroundChar = backgroundChar;
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }
}
