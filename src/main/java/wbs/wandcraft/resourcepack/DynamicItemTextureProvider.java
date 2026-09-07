package wbs.wandcraft.resourcepack;

import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.resourcepack.ResourcePackObjects.ModelReference;
import wbs.wandcraft.resourcepack.ResourcePackObjects.ModelTint;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static wbs.wandcraft.resourcepack.ResourcePackObjects.ItemModel;
import static wbs.wandcraft.resourcepack.ResourcePackObjects.StaticModelReference;

public interface DynamicItemTextureProvider extends ItemModelProvider {
    @NotNull
    List<TextureLayer> getTextures();
    @NotNull
    default List<ModelTint> getTints() {
        List<TextureLayer> textures = getTextures();
        List<ModelTint> tints = new LinkedList<>();

        for (int i = 0; i < textures.size(); i++) {
            TextureLayer layer = textures.get(i);

            if (layer.defaultTint() != null) {
                tints.add(new ModelTint(i, layer.defaultTint()));
            }
        }

        return tints;
    }
    @NotNull
    default List<String> getLayerResourceLocations() {
        return getTextures().stream()
                .map(layer -> namespace() + ":" + layer.folder() + "/" + layer.name())
                .toList();
    }

    @Override
    default ModelReference buildBaseModel() {
        return new StaticModelReference(namespace() + ":item/" + modelResourceLocation(), getTints());
    }

    default Map<String, ItemModel> getModelDefinitions() {
        Map<String, ItemModel> namedModelDefinitions = new HashMap<>();

        ItemModel modelDefinition = new ItemModel(
                getModelParent(),
                getLayerResourceLocations()
        );

        namedModelDefinitions.put(value(), modelDefinition);

        return namedModelDefinitions;
    }

    default @NotNull String getModelParent() {
        return "minecraft:item/generated";
    }
}
