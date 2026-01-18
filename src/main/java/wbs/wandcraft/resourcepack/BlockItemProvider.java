package wbs.wandcraft.resourcepack;

import wbs.wandcraft.resourcepack.ResourcePackObjects.Model;

import static wbs.wandcraft.resourcepack.ResourcePackObjects.StaticModel;

public interface BlockItemProvider extends ItemModelProvider {
    @Override
    default Model buildBaseModel() {
        return new StaticModel(namespace() + ":block/" + value());
    }
}
