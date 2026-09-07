package wbs.wandcraft.resourcepack;

import com.google.gson.annotations.SerializedName;
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

    //region ModelReference
    public abstract static class ModelReference {
        private final String type;

        private Transformation transformation;

        public ModelReference(String type) {
            this.type = type;
        }

        public ModelReference transformation(Transformation transformation) {
            this.transformation = transformation;
            return this;
        }
    }

    @SuppressWarnings("MismatchedReadAndWriteOfArray")
    public static class Transformation {
        private final double[] translation = new double[3];
        private final double[] left_rotation = new double[] {0, 0, 0, 1};
        private final double[] right_rotation = new double[] {0, 0, 0, 1};
        private final double[] scale = new double[] {1, 1, 1};

        public Transformation() {}

        public Transformation leftRotation(double x, double y, double z, double w) {
            this.left_rotation[0] = x;
            this.left_rotation[1] = y;
            this.left_rotation[2] = z;
            this.left_rotation[3] = w;

            return this;
        }

        public Transformation rightRotation(double x, double y, double z, double w) {
            this.right_rotation[0] = x;
            this.right_rotation[1] = y;
            this.right_rotation[2] = z;
            this.right_rotation[3] = w;

            return this;
        }

        public Transformation translation(double x, double y, double z) {
            this.translation[0] = x;
            this.translation[1] = y;
            this.translation[2] = z;

            return this;
        }

        public Transformation scale(double x, double y, double z) {
            this.scale[0] = x;
            this.scale[1] = y;
            this.scale[2] = z;

            return this;
        }
    }

    public static class SelectModelReference extends ModelReference {
        private final String property;
        private final int index;
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        private final List<ModelCase> cases = new LinkedList<>();
        private final ModelReference fallback;

        public SelectModelReference(String property, ModelReference fallback) {
            this(property, -1, fallback);
        }
        public SelectModelReference(String property, int index, ModelReference fallback) {
            super("select");
            this.property = property;
            this.index = index;
            this.fallback = fallback;
        }

        public void addCase(ModelCase modelCase) {
            cases.add(modelCase);
        }
    }

    public static class ConditionModelReference extends ModelReference {
        private final String property;
        private final ModelReference on_true;
        private final ModelReference on_false;

        public ConditionModelReference(String property, ModelReference onTrue, ModelReference onFalse) {
            super("condition");
            this.property = property;
            on_true = onTrue;
            on_false = onFalse;
        }
    }

    public static class CompositeModelReference extends ModelReference {
        private final List<ModelReference> models;

        public CompositeModelReference(List<ModelReference> models) {
            super("composite");
            this.models = models;
        }
        public CompositeModelReference(ModelReference... models) {
            this(Arrays.asList(models));
        }
    }

    public static class StaticModelReference extends ModelReference {
        private final String model;
        @Nullable
        private List<ModelTint> tints;

        public StaticModelReference(String model) {
            this(model, null);
        }
        public StaticModelReference(String model, @Nullable List<ModelTint> tints) {
            super("minecraft:model");
            this.model = model;
            this.tints = tints;
        }

        public StaticModelReference addTint(ModelTint tint) {
            if (tints == null) {
                tints = new LinkedList<>();
            }

            tints.add(tint);
            return this;
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
        private final ModelReference model;

        public ModelCase(String when, ModelReference model) {
            this.when = when;
            this.model = model;
        }
    }
    //endregion Model

    //region Item
    public static final class ItemDefinition {
        private final ModelReference model;
        @Nullable
        private Boolean oversized_in_gui = null;
        // hand animation on swap
        // swap animation scale

        public ItemDefinition(ModelReference model) {
            this.model = model;
        }

        public ItemDefinition setOversizedInGUI(boolean oversizedInGUI) {
            this.oversized_in_gui = oversizedInGUI;
            return this;
        }
    }

    public static final class ItemModel {
        private final String parent;
        private final Map<String, String> textures = new HashMap<>();
        private Map<String, DisplayTransform> display;

        public ItemModel(String parent, String ... textures) {
            this(parent, Arrays.asList(textures));
        }
        public ItemModel(String parent, List<String> textures) {
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
            transforms.forEach((val, transform) -> {
                display.put(val.name().toLowerCase(), transform);
            });
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
    //endregion Item

    //region Font
    public static final class Font {
        private final FontProvider[] providers;

        public Font(FontProvider[] providers) {
            this.providers = providers;
        }
    }

    public static class FontProvider {
        private final String type;

        public FontProvider(String type) {
            this.type = type;
        }
    }

    public static class BitmapFontProvider extends FontProvider {
        private final String file;
        private final int ascent;
        private final int height;
        private final String[] chars;

        public BitmapFontProvider(String file, int ascent, int height, String[] chars) {
            super("bitmap");
            this.file = file;
            this.ascent = ascent;
            this.height = height;
            this.chars = chars;
        }
    }

    public static class SpaceFontProvider extends FontProvider {
        private final Map<Character, Integer> advances;

        public SpaceFontProvider(Map<Character, Integer> advances) {
            super("space");
            this.advances = advances;
        }
    }
    //endregion
}
