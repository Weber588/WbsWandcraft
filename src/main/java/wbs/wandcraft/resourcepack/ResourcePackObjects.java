package wbs.wandcraft.resourcepack;

import com.google.gson.annotations.SerializedName;
import net.kyori.adventure.key.Key;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Objects that may be gson-serialized into resource pack objects.
 */
// Suppress unused/can be local/updated but not queried warnings, as they aren't applicable for serialization.
@SuppressWarnings({"FieldCanBeLocal", "unused", "MismatchedQueryAndUpdateOfCollection"})
public final class ResourcePackObjects {
    private ResourcePackObjects() {}

    public interface ModelDefinition {}

    public static final class ItemModelDefinition implements ModelDefinition {
        private final String parent;
        private final Map<String, String> textures = new HashMap<>();
        private Map<String, DisplayTransform> display;

        public ItemModelDefinition(String parent, String ... textures) {
            this(parent, Arrays.asList(textures));
        }
        public ItemModelDefinition(String parent, List<String> textures) {
            this.parent = parent;
            for (int i = 0; i < textures.size(); i++) {
                this.textures.put("layer" + i, textures.get(i));
            }
        }

        public void addDisplay(ItemDisplayTransform displayType, DisplayTransform transform) {
            if (display == null) {
                display = new HashMap<>();
            }
            display.put(displayType.name().toLowerCase(), transform);
        }

        public void defaultDisplay(DisplayTransform transform) {
            if (display == null) {
                display = new HashMap<>();
            }

            for (ItemDisplayTransform value : ItemDisplayTransform.values()) {
                display.putIfAbsent(value.name().toLowerCase(), transform);
            }
        }

        public void setDisplays(Map<ItemDisplayTransform, DisplayTransform> transforms) {
            if (display == null) {
                display = new HashMap<>();
            }

            display.clear();
            transforms.forEach((value, transform) -> {
                display.put(value.name().toLowerCase(), transform);
            });
        }
    }

    public interface ItemDefinition {}

    public static final class ItemSelectorDefinition implements ItemDefinition {
        private final Model model;

        public ItemSelectorDefinition(Material baseMaterial, List<? extends ItemModelProvider> definitions, boolean isBlock) {
            String fallbackType = "item";
            if (isBlock) {
                fallbackType = "block";
            }
            StaticModel fallback = new StaticModel("minecraft:" + fallbackType + "/" + baseMaterial.key().value(), null);

            SelectModel model = new SelectModel("custom_model_data", 0, fallback);

            for (ItemModelProvider definition : definitions) {
                Key key = definition.key();
                model.addCase(new ModelCase(
                        key.asString(),
                        definition.buildBaseModel()
                ));
            }

            this.model = model;
        }
        public ItemSelectorDefinition(Model model) {
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

        public void addCase(ModelCase modelCase) {
            cases.add(modelCase);
        }
    }

    public static class ConditionModel extends Model {
        private final String property;
        private final Model on_true;
        private final Model on_false;

        public ConditionModel(String property, Model onTrue, Model onFalse) {
            super("condition");
            this.property = property;
            on_true = onTrue;
            on_false = onFalse;
        }
    }

    public static class CompositeModel extends Model {
        private final List<Model> models;

        public CompositeModel(List<Model> models) {
            super("composite");
            this.models = models;
        }
        public CompositeModel(Model ... models) {
            this(Arrays.asList(models));
        }
    }

    public static class StaticModel extends Model {
        private final String model;
        @Nullable
        private final List<ModelTint> tints;

        public StaticModel(String model) {
            this(model, null);
        }
        public StaticModel(String model, @Nullable List<ModelTint> tints) {
            super("minecraft:model");
            this.model = model;
            this.tints = tints;
        }
    }

    public static final class ModelTint {
        private final String type = "minecraft:custom_model_data";
        private final int index;
        @SerializedName("default")
        private final int defaultValue;

        public ModelTint(int index, int defaultValue) {
            this.index = index;
            this.defaultValue = defaultValue;
        }
    }

    public static final class ModelCase {
        private final String when;
        private final Model model;

        public ModelCase(String when, Model model) {
            this.when = when;
            this.model = model;
        }
    }

    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    public static final class DisplayTransform {
        private double[] rotation;
        private double[] translation;
        private double[] scale;

        public DisplayTransform() {}

        public DisplayTransform rotation(double x, double y, double z) {
            this.rotation = new double[3];
            this.rotation[0] = x;
            this.rotation[1] = y;
            this.rotation[2] = z;

            return this;
        }

        public DisplayTransform translation(double x, double y, double z) {
            this.translation = new double[3];
            this.translation[0] = x;
            this.translation[1] = y;
            this.translation[2] = z;

            return this;
        }

        public DisplayTransform scale(double x, double y, double z) {
            this.scale = new double[3];
            this.scale[0] = x;
            this.scale[1] = y;
            this.scale[2] = z;

            return this;
        }
    }
}
