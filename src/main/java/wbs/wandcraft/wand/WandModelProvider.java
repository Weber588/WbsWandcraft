package wbs.wandcraft.wand;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.resourcepack.ItemModelProvider;

public abstract class WandModelProvider implements Keyed, ItemModelProvider {
    private final NamespacedKey key;

    protected WandModelProvider(NamespacedKey key) {
        this.key = key;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
