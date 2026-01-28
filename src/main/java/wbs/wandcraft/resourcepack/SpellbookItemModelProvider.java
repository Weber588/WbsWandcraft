package wbs.wandcraft.resourcepack;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpellbookItemModelProvider implements ExternalItemProvider {
    @Override
    public ResourcePackObjects.Model buildBaseModel() {
        return new ResourcePackObjects.ConditionModel(
                "using_item",
                new ResourcePackObjects.CompositeModel(
                        buildAppendedModel("_open"),
                        buildAppendedModel("_open_overlay")
                                .addTint(new ResourcePackObjects.ModelTint(0, 0)),
                        buildAppendedModel("_open_overlay_highlight")
                                .addTint(new ResourcePackObjects.ModelTint(1, 0))
                ),
                ExternalItemProvider.super.buildBaseModel()
        );
    }

    @Override
    public @NotNull String getModelType() {
        return "item";
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return new NamespacedKey("wbswandcraft", "spellbook");
    }

    @Override
    public List<String> getAdditionalModels() {
        return List.of(
                value() + "_open",
                value() + "_open_overlay",
                value() + "_open_overlay_highlight"
        );
    }

    @Override
    public @Nullable String credit() {
        return "Weber588";
    }
}
