package wbs.wandcraft.util;

import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.WbsRegistry;

import java.util.function.Function;

public class ItemBuildableRegistry<T extends Keyed> extends WbsRegistry<T> {
    private final Function<T, ItemStack> builder;

    public ItemBuildableRegistry(Function<T, ItemStack> builder) {
        this.builder = builder;
    }

    @SafeVarargs
    public ItemBuildableRegistry(Function<T, ItemStack> builder, T... initial) {
        super(initial);
        this.builder = builder;
    }

    public ItemBuildableRegistry(Function<T, ItemStack> builder, Iterable<T> initial) {
        super(initial);
        this.builder = builder;
    }

    @Nullable
    public ItemStack getDefaultItem(Key key) {
        T value = get(key);
        if (value != null) {
            return builder.apply(value);
        }
        return null;
    }
}
