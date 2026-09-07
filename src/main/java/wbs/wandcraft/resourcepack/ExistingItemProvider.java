package wbs.wandcraft.resourcepack;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.resourcepack.ResourcePackObjects.ModelReference;

import static wbs.wandcraft.resourcepack.ResourcePackObjects.StaticModelReference;

public class ExistingItemProvider implements ItemModelProvider {
    private final NamespacedKey key;
    private final String modelType;

    public ExistingItemProvider(NamespacedKey key, String modelType) {
        this.key = key;
        this.modelType = modelType;
    }

    @Override
    public ModelReference buildBaseModel() {
        return new StaticModelReference(namespace() + ":" + getModelType() + "/" + value());
    }

    @NotNull
    public String getModelType() {
        return modelType;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
