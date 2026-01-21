package wbs.wandcraft.resourcepack;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SpellbookItemModelProvider implements BlockItemProvider {
    @Override
    public ResourcePackObjects.Model buildBaseModel() {
        return new ResourcePackObjects.ConditionModel(
                "using_item",
                new ResourcePackObjects.StaticModel(namespace() + ":block/" + value() + "_open"),
                BlockItemProvider.super.buildBaseModel()
        );
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return new NamespacedKey("wbswandcraft", "spellbook");
    }

    @Override
    public List<String> getAdditionalModels() {
        return List.of(value() + "_open");
    }

    @Override
    public @Nullable String credit() {
        return "Weber588";
    }
}
