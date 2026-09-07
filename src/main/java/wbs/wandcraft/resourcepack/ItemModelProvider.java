package wbs.wandcraft.resourcepack;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import org.bukkit.Keyed;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

import static wbs.wandcraft.resourcepack.ResourcePackObjects.ModelReference;

public interface ItemModelProvider extends Keyed {
    default String namespace() {
        return key().namespace();
    }
    default @Nullable String subfolder() {
        return null;
    }
    default String value() {
        return key().value();
    }

    ModelReference buildBaseModel();

    @Nullable
    default String credit() {
        return null;
    }

    /**
     * @return The resource location for the model definition
     */
    default String modelResourceLocation() {
        String subfolder = subfolder();
        String targetPath = getKey().value();
        if (subfolder != null) {
            targetPath = subfolder + "/" + targetPath;
        }
        return targetPath;
    }

    default String modelKey() {
        String subfolder = subfolder();
        String targetPath = getKey().value();
        if (subfolder != null) {
            targetPath = subfolder + "/" + targetPath;
        }
        return namespace() + ":" + targetPath;
    }

    default void applyCustomModelData(ItemStack item) {
        applyCustomModelData(item, ignored -> {});
    }
    default void applyCustomModelData(ItemStack item, Consumer<CustomModelData.Builder> consumer) {
        CustomModelData.Builder builder = CustomModelData.customModelData().addString(modelKey());
        consumer.accept(builder);
        item.setData(
                DataComponentTypes.CUSTOM_MODEL_DATA,
                builder.build()
        );
    }
}
