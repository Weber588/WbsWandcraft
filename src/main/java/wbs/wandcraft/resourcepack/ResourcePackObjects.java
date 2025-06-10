package wbs.wandcraft.resourcepack;

import com.google.gson.annotations.SerializedName;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.TextureProvider;

import java.util.*;

/**
 * Objects that may be gson-serialized into resource pack objects.
 */
// Suppress unused/can be local/updated but not queried warnings, as they aren't applicable for serialization.
@SuppressWarnings({"FieldCanBeLocal", "unused", "MismatchedQueryAndUpdateOfCollection"})
public final class ResourcePackObjects {
    private ResourcePackObjects() {}

    public static final class ModelDefinition {
        private final String parent;
        private final Map<String, String> textures = new HashMap<>();

        public ModelDefinition(String parent, String ... textures) {
            this(parent, Arrays.asList(textures));
        }
        public ModelDefinition(String parent, List<String> textures) {
            this.parent = parent;
            for (int i = 0; i < textures.size(); i++) {
                this.textures.put("layer" + i, textures.get(i));
            }
        }
    }

    public static final class ItemSelectorDefinition {
        private final Model model;

        public ItemSelectorDefinition(Material baseMaterial, List<? extends TextureProvider> definitions) {
            this(baseMaterial, definitions, null);
        }
        public ItemSelectorDefinition(Material baseMaterial, List<? extends TextureProvider> definitions, @Nullable List<ModelTint> tints) {
            StaticModel fallback = new StaticModel("minecraft:item/" + baseMaterial.key().value(), null);

            SelectModel model = new SelectModel("custom_model_data", 0, fallback);

            for (TextureProvider definition : definitions) {
                Key key = definition.key();
                model.addCase(new ModelCase(
                        key.asString(),
                        new StaticModel(key.namespace() + ":item/" + key.value(), tints)
                ));
            }

            this.model = model;
        }
    }

    public abstract static class Model {
        private final String type;

        public Model(String type) {
            this.type = type;
        }
    }

    public static class SelectModel extends Model {
        private final String property;
        private final int index;
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        private final List<ModelCase> cases = new LinkedList<>();
        private final Model fallback;

        public SelectModel(String property, int index, Model fallback) {
            super("select");
            this.property = property;
            this.index = index;
            this.fallback = fallback;
        }

        private void addCase(ModelCase modelCase) {
            cases.add(modelCase);
        }
    }

    public static class StaticModel extends Model {
        private final String model;
        private final List<ModelTint> tints;

        public StaticModel(String model, List<ModelTint> tints) {
            super("minecraft:model");
            this.model = model;
            this.tints = tints;
        }
    }

    public static final class ModelTint {
        private final String type = "minecraft:custom_model_data";
        private final int index = 0;
        @SerializedName("default")
        private final int defaultValue = 32768;
    }

    public static final class ModelCase {
        private final String when;
        private final Model model;

        public ModelCase(String when, Model model) {
            this.when = when;
            this.model = model;
        }
    }
}
