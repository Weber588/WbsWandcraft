package wbs.wandcraft.resourcepack;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpellbookItemModelProvider implements ExternalModelProvider {
    @Override
    public ResourcePackObjects.Model buildBaseModel() {
        return new ResourcePackObjects.ConditionModel(
                "using_item",
                new ResourcePackObjects.CompositeModel(
                        new ResourcePackObjects.StaticModel(namespace() + ":item/" + value() + "_open"),
                        new ResourcePackObjects.StaticModel(namespace() + ":item/" + value() + "_open_overlay", List.of(
                                new ResourcePackObjects.ModelTint(0, 0)
                        )),
                        new ResourcePackObjects.StaticModel(namespace() + ":item/" + value() + "_open_overlay_highlight", List.of(
                                new ResourcePackObjects.ModelTint(1, 0)
                        ))
                ),
                new ResourcePackObjects.StaticModel(namespace() + ":item/" + value())
        );
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return new NamespacedKey("wbswandcraft", "spellbook");
    }

    @Override
    public List<String> getResources() {
        return List.of(
                ResourcePackBuilder.ITEM_TEXTURES_PATH + value() + ".png",
                ResourcePackBuilder.ITEM_MODELS_PATH + value() + ".json",
                ResourcePackBuilder.ITEM_MODELS_PATH + value() + "_open.json",
                ResourcePackBuilder.ITEM_MODELS_PATH + value() + "_open_overlay.json",
                ResourcePackBuilder.ITEM_MODELS_PATH + value() + "_open_overlay_highlight.json"
        );
    }

    @Override
    public @Nullable String credit() {
        return "Weber588";
    }
}
