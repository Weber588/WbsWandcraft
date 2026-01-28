package wbs.wandcraft.resourcepack;

import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.resourcepack.ResourcePackObjects.Model;

import java.util.List;

import static wbs.wandcraft.resourcepack.ResourcePackObjects.StaticModel;

public interface ExternalItemProvider extends ItemModelProvider {
    @Override
    default Model buildBaseModel() {
        return buildAppendedModel("");
    }

    default StaticModel buildAppendedModel(String appendWith) {
        return new StaticModel(namespace() + ":" + getModelType() + "/" + value() + appendWith);
    }

    @NotNull String getModelType();

    default List<String> getAdditionalModels() {
        return List.of();
    }
    default List<String> getAdditionalTextures() {
        return List.of();
    }
}
