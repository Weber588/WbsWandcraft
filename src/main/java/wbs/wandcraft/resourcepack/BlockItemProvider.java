package wbs.wandcraft.resourcepack;

import wbs.wandcraft.resourcepack.ResourcePackObjects.Model;

import java.util.List;

import static wbs.wandcraft.resourcepack.ResourcePackObjects.StaticModel;

public interface BlockItemProvider extends ItemModelProvider {
    @Override
    default Model buildBaseModel() {
        return new StaticModel(namespace() + ":block/" + value());
    }

    default List<String> getAdditionalModels() {
        return List.of();
    }
    default List<String> getAdditionalTextures() {
        return List.of();
    }
}
