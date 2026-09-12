package wbs.wandcraft;

import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.extensions.TargetedSpell;
import wbs.wandcraft.util.persistent.CustomPersistentDataTypes;

@NullMarked
public record AttributeDataType<T>(NamespacedKey key, PersistentDataType<?, T> dataType) implements Keyed {
    public static final AttributeDataType<Integer> INTEGER
            = new AttributeDataType<>("int", PersistentDataType.INTEGER);
    public static final AttributeDataType<Boolean> BOOLEAN
            = new AttributeDataType<>("boolean", PersistentDataType.BOOLEAN);
    public static final AttributeDataType<Double> DOUBLE
            = new AttributeDataType<>("double", PersistentDataType.DOUBLE);
    public static final AttributeDataType<String> STRING
            = new AttributeDataType<>("string", PersistentDataType.STRING);
    public static final AttributeDataType<Long> LONG
            = new AttributeDataType<>("long", PersistentDataType.LONG);
    public static final AttributeDataType<Particle> PARTICLE
            = new AttributeDataType<>("particle", new CustomPersistentDataTypes.PersistentEnumType<>(Particle.class));
    public static final AttributeDataType<Material> MATERIAL
            = new AttributeDataType<>("material", new CustomPersistentDataTypes.PersistentEnumType<>(Material.class));
    public static final AttributeDataType<TargetedSpell.TargeterType> TARGETER
            = new AttributeDataType<>("targeter", new CustomPersistentDataTypes.PersistentEnumType<>(TargetedSpell.TargeterType.class));
    public static final AttributeDataType<SpellInstance> SPELL
            = new AttributeDataType<>("spell_instance", CustomPersistentDataTypes.SPELL_INSTANCE);

    public AttributeDataType(String nativeKey, PersistentDataType<?, T> dataType) {
        this(WbsWandcraft.getKey(nativeKey), dataType);

        WandcraftRegistries.DATA_TYPES.register(this);
    }
    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
