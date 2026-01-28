package wbs.wandcraft.equipment.hat;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.resourcepack.ExternalItemProvider;
import wbs.wandcraft.resourcepack.ResourcePackObjects;

import java.util.List;

public final class HatModel implements ExternalItemProvider {
    public static final HatModel WITCH = new HatModel("witch");
    public static final HatModel APPRENTICE = new HatModel("apprentice", "SolariumYT");
    public static final HatModel ARCANIST = new HatModel("arcanist", "SolariumYT");
    public static final HatModel DRUID = new HatModel("druid", "SolariumYT");
    public static final HatModel FIREMANCER = new HatModel("firemancer", "SolariumYT");
    public static final HatModel HEALER = new HatModel("healer", "SolariumYT");
    public static final HatModel MARKSMAN = new HatModel("marksman", "SolariumYT");
    public static final HatModel OLD = new HatModel("old", "SolariumYT");
    public static final HatModel SEER = new HatModel("seer", "SolariumYT");
    public static final HatModel SORCERER = new HatModel("sorcerer", "SolariumYT");
    public static final HatModel SPEEDSTER = new HatModel("speedster", "SolariumYT");
    public static final HatModel SPELLSLINGER = new HatModel("spellslinger", "SolariumYT");
    public static final HatModel WARLOCK = new HatModel("warlock", "SolariumYT");

    private final NamespacedKey key;
    @Nullable
    private final String credit;

    public HatModel(String textureKey) {
        this(textureKey, null);
    }
    public HatModel(String textureKey, @Nullable String credit) {
        this.key = WbsWandcraft.getKey("hat_" + textureKey);
        this.credit = credit;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public ResourcePackObjects.Model buildBaseModel() {
        ResourcePackObjects.SelectModel selectModel = new ResourcePackObjects.SelectModel(
                "minecraft:context_entity_type",
                ExternalItemProvider.super.buildBaseModel()
        );

        selectModel.addCase(new ResourcePackObjects.ModelCase(
                "minecraft:evoker",
                new ResourcePackObjects.StaticModel(namespace() + ":" + getModelType() + "/" + value() + "_evoker")
        ));

        return selectModel;
    }

    @Override
    public @NotNull String getModelType() {
        return "block";
    }

    @Override
    public List<String> getAdditionalModels() {
        return List.of(value() + "_evoker");
    }

    @Override
    public @Nullable String credit() {
        return credit;
    }
}
