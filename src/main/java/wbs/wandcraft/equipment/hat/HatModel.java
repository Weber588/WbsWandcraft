package wbs.wandcraft.equipment.hat;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.resourcepack.BlockItemProvider;
import wbs.wandcraft.resourcepack.ResourcePackObjects;

import java.util.List;
import java.util.Map;

import static wbs.wandcraft.resourcepack.ResourcePackObjects.DisplayTransform;

public final class HatModel implements BlockItemProvider {
    public static final HatModel WITCH = new HatModel("witch");
    public static final HatModel APPRENTICE = new HatModel("apprentice");
    public static final HatModel ARCANIST = new HatModel("arcanist");
    public static final HatModel DRUID = new HatModel("druid");
    public static final HatModel FIREMANCER = new HatModel("firemancer");
    public static final HatModel HEALER = new HatModel("healer");
    public static final HatModel MARKSMAN = new HatModel("marksman");
    public static final HatModel OLD = new HatModel("old");
    public static final HatModel SEER = new HatModel("seer");
    public static final HatModel SORCERER = new HatModel("sorcerer");
    public static final HatModel SPEEDSTER = new HatModel("speedster");
    public static final HatModel SPELLSLINGER = new HatModel("spellslinger");
    public static final HatModel WARLOCK = new HatModel("warlock");

    private final NamespacedKey key;

    private Map<ItemDisplayTransform, DisplayTransform> displays;
    private Map<ItemDisplayTransform, DisplayTransform> inUseDisplay;

    public HatModel(String textureKey) {
        this.key = WbsWandcraft.getKey("hat_" + textureKey);
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public ResourcePackObjects.Model buildBaseModel() {
        ResourcePackObjects.SelectModel selectModel = new ResourcePackObjects.SelectModel(
                "minecraft:context_entity_type",
                -1,
                BlockItemProvider.super.buildBaseModel()
        );

        selectModel.addCase(new ResourcePackObjects.ModelCase(
                "minecraft:evoker",
                new ResourcePackObjects.StaticModel(namespace() + ":block/" + value() + "_evoker")
        ));

        return selectModel;
    }

    @Override
    public List<String> getAdditionalModels() {
        return List.of(value() + "_evoker");
    }
}
