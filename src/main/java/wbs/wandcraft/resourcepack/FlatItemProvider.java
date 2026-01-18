package wbs.wandcraft.resourcepack;

import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.resourcepack.ResourcePackObjects.Model;
import wbs.wandcraft.resourcepack.ResourcePackObjects.ModelTint;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static wbs.wandcraft.resourcepack.ResourcePackObjects.ItemModelDefinition;
import static wbs.wandcraft.resourcepack.ResourcePackObjects.StaticModel;

public interface FlatItemProvider extends ItemModelProvider {
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
                .map(layer -> namespace() + ":item/" + layer.name())
                .toList();
    }

    @Override
    default Model buildBaseModel() {
        return new StaticModel(namespace() + ":item/" + value(), getTints());
    }

    default Map<String, ItemModelDefinition> getModelDefinitions() {
        Map<String, ItemModelDefinition> namedModelDefinitions = new HashMap<>();

        ItemModelDefinition modelDefinition = new ItemModelDefinition(
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
