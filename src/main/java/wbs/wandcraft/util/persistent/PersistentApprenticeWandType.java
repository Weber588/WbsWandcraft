package wbs.wandcraft.util.persistent;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.wand.types.ApprenticeWand;

public class PersistentApprenticeWandType extends AbstractPersistentWandType<ApprenticeWand> {
    private static final NamespacedKey LEFT = WbsWandcraft.getKey("left");
    private static final NamespacedKey RIGHT = WbsWandcraft.getKey("right");

    @Override
    public @NotNull Class<ApprenticeWand> getComplexType() {
        return ApprenticeWand.class;
    }

    @Override
    protected void writeTo(PersistentDataContainer container, ApprenticeWand wand, @NotNull PersistentDataAdapterContext context) {
        ItemStack left = wand.getLeft();
        ItemStack right = wand.getRight();
        if (left != null) {
            container.set(LEFT, WbsPersistentDataType.ITEM_AS_BYTES, left);
        }
        if (right != null) {
            container.set(RIGHT, WbsPersistentDataType.ITEM_AS_BYTES, right);
        }
    }

    @Override
    protected @NotNull ApprenticeWand getWand(@NotNull PersistentDataContainer container, @NotNull String uuid) {
        return new ApprenticeWand(uuid);
    }

    @Override
    protected void populateWand(ApprenticeWand wand, @NotNull PersistentDataContainer container) {
        ItemStack left = container.get(LEFT, WbsPersistentDataType.ITEM_AS_BYTES);
        ItemStack right = container.get(RIGHT, WbsPersistentDataType.ITEM_AS_BYTES);

        wand.setLeft(left);
        wand.setRight(right);
    }
}
