package wbs.wandcraft.wand;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.resourcepack.ExternalItemProvider;
import wbs.wandcraft.resourcepack.ResourcePackObjects;

import java.util.List;

public abstract class ExternalWandModel extends WandModelProvider implements ExternalItemProvider {
    public static final ExternalWandModel BROOMSTICK = new ExternalWandModel("broomstick") {
        @Override
        public ResourcePackObjects.Model buildBaseModel() {
            return new ResourcePackObjects.ConditionModel(
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

    protected ExternalWandModel(String nativeKey) {
        this(WbsWandcraft.getKey(nativeKey));
    }
    protected ExternalWandModel(NamespacedKey key) {
        super(key);
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
