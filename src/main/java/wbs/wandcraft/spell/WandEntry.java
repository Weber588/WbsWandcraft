package wbs.wandcraft.spell;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import wbs.wandcraft.ItemDecorator;

/**
 * Describes an object that can be added to a wand to have an effect.
 */
public interface WandEntry<T extends WandEntry<T>> extends ItemDecorator {
    default void toItem(ItemStack item) {
        item.editMeta(meta -> {
            meta.getPersistentDataContainer().set(getTypeKey(), getThisType(), getThis());
            ItemDecorator.decorate(this, meta);
        });
    }

    NamespacedKey getTypeKey();
    PersistentDataType<?, T> getThisType();
    default T getThis() {
        //noinspection unchecked
        return (T) this;
    }
}
