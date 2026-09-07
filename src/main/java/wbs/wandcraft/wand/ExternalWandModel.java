package wbs.wandcraft.wand;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.resourcepack.ExternalItemProvider;
import wbs.wandcraft.resourcepack.ResourcePackObjects;

import java.util.List;

public abstract class ExternalWandModel extends WandModelProvider implements ExternalItemProvider {
    public static final ExternalWandModel BROOMSTICK = new ExternalWandModel("broomstick", '\uE780') {
        @Override
        public ResourcePackObjects.ModelReference buildBaseModel() {
            return new ResourcePackObjects.ConditionModelReference(
                    "using_item",
                    buildAppendedModel("_backwards"),
                    super.buildBaseModel()
            );
        }

        @Override
        public List<String> getAdditionalModels() {
            return List.of(
                    value() + "_backwards"
            );
        }
    };

    protected ExternalWandModel(String nativeKey, char backgroundChar) {
        this(WbsWandcraft.getKey(nativeKey), backgroundChar);
    }
    protected ExternalWandModel(NamespacedKey key, char backgroundChar) {
        super(key, backgroundChar);
    }

    @Override
    public @NotNull String getModelType() {
        return "item";
    }

    @Override
    public String value() {
        return "wand_" + super.value();
    }
}
