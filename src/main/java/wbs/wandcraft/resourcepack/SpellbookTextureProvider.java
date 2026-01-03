package wbs.wandcraft.resourcepack;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.ItemDisplay;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpellbookTextureProvider implements TextureProvider {
    @Override
    public ResourcePackObjects.Model buildBaseModel() {
        return new ResourcePackObjects.ConditionModel(
                "using_item",
                new ResourcePackObjects.StaticModel(namespace() + ":item/" + textureName() + "_active", getTints()),
                TextureProvider.super.buildBaseModel()
        );
    }

    @Override
    public Map<String, ResourcePackObjects.ModelDefinition> getModelDefinitions() {
        Map<String, ResourcePackObjects.ModelDefinition> namedModelDefinitions = new HashMap<>();

        ResourcePackObjects.ModelDefinition baseModelDefinition = new ResourcePackObjects.ModelDefinition(
                "minecraft:item/generated",
                namespace() + ":item/" + textureName()
        );

        namedModelDefinitions.put(textureName(), baseModelDefinition);

        ResourcePackObjects.ModelDefinition activeModelDefinition = new ResourcePackObjects.ModelDefinition(
                "minecraft:item/generated",
                namespace() + ":item/" + textureName() + "_active"
        );

        ResourcePackObjects.DisplayTransform rightTransform = new ResourcePackObjects.DisplayTransform()
                .translation(1.13, 0.2, 3.13)
                .rotation(65, -90, 0)
                .scale(0.7, 0.7, 0.7);

        ResourcePackObjects.DisplayTransform leftTransform = new ResourcePackObjects.DisplayTransform()
                .translation(1.13, 0.2, 3.13)
                .rotation(80, -90, 0)
                .scale(0.7, 0.7, 0.7);

        ResourcePackObjects.DisplayTransform right3rdTransform = new ResourcePackObjects.DisplayTransform()
                .translation(0, 4, 1)
                .scale(0.75, 0.75, 0.75);

        ResourcePackObjects.DisplayTransform left3rdTransform = new ResourcePackObjects.DisplayTransform()
                .translation(1.13, 4.2, 1.13)
                .scale(0.75, 0.75, 0.75);

        activeModelDefinition.addDisplay(ItemDisplay.ItemDisplayTransform.FIRSTPERSON_RIGHTHAND, rightTransform);
        activeModelDefinition.addDisplay(ItemDisplay.ItemDisplayTransform.FIRSTPERSON_LEFTHAND, leftTransform);
        activeModelDefinition.addDisplay(ItemDisplay.ItemDisplayTransform.THIRDPERSON_RIGHTHAND, right3rdTransform);
        activeModelDefinition.addDisplay(ItemDisplay.ItemDisplayTransform.THIRDPERSON_LEFTHAND, left3rdTransform);

        namedModelDefinitions.put(textureName() + "_active", activeModelDefinition);

        return namedModelDefinitions;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return new NamespacedKey("wbswandcraft", "spellbook");
    }

    @Override
    public @NotNull List<TextureLayer> getTextures() {
        return List.of(
                new TextureLayer("spellbook"),
                new TextureLayer("spellbook_active")
        );
    }
}
