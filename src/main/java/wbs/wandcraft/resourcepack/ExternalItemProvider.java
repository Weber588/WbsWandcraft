package wbs.wandcraft.resourcepack;

import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.resourcepack.ResourcePackObjects.ModelReference;

import java.util.List;

import static wbs.wandcraft.resourcepack.ResourcePackObjects.StaticModelReference;

public interface ExternalItemProvider extends ItemModelProvider {
    @Override
    default ModelReference buildBaseModel() {
        return buildAppendedModel("");
    }

    default StaticModelReference buildAppendedModel(String appendWith) {
        return new StaticModelReference(namespace() + ":" + getModelType() + "/" + value() + appendWith);
    }

    @NotNull String getModelType();

    default boolean externalTexture() {
        return true;
    }

    default List<String> getAdditionalModels() {
        return List.of();
    }
    default List<String> getAdditionalTextures() {
        return List.of();
    }
}
