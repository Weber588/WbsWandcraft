package wbs.wandcraft.util;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import wbs.wandcraft.WbsWandcraft;

public class ConfiguredBlockDisplay {
    public static NamespacedKey TAG = WbsWandcraft.getKey("configured_name");

    private final String name;
    private final BlockData blockData;
    private final Vector offset;

    private final Transformation transformation;

    public ConfiguredBlockDisplay(@NotNull String name, BlockData blockData, Vector offset, Vector scale) {
        this.name = name;
        this.blockData = blockData;
        this.offset = offset;

        Vector3f translation = new Vector3f((float) (-0.5 * scale.getX()), 0, (float) (-0.5 * scale.getZ()));
        AxisAngle4f zeroed = new AxisAngle4f(0f, 0f, 0f, 0f);
        transformation = new Transformation(translation, zeroed, scale.toVector3f(), zeroed);
    }

    public BlockDisplay spawn(Location origin, NamespacedKey parentKey) {
        return origin.getWorld().spawn(origin.clone().add(offset), BlockDisplay.class, display -> {
            display.setBlock(blockData);
            display.setTransformation(transformation);
            display.getPersistentDataContainer().set(parentKey, PersistentDataType.STRING, name);
        });
    }
}
