package wbs.wandcraft;

import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.util.CustomPersistentDataTypes;

@NullMarked
public record RegisteredPersistentDataType<T>(NamespacedKey key, PersistentDataType<?, T> dataType) implements Keyed {
    public static final RegisteredPersistentDataType<Integer> INTEGER
            = new RegisteredPersistentDataType<>("int", PersistentDataType.INTEGER);
    public static final RegisteredPersistentDataType<Boolean> BOOLEAN
            = new RegisteredPersistentDataType<>("boolean", PersistentDataType.BOOLEAN);
    public static final RegisteredPersistentDataType<Double> DOUBLE
            = new RegisteredPersistentDataType<>("double", PersistentDataType.DOUBLE);
    public static final RegisteredPersistentDataType<String> STRING
            = new RegisteredPersistentDataType<>("string", PersistentDataType.STRING);
    public static final RegisteredPersistentDataType<Long> LONG
            = new RegisteredPersistentDataType<>("long", PersistentDataType.LONG);
    public static final RegisteredPersistentDataType<Particle> PARTICLE
            = new RegisteredPersistentDataType<>("particle", new CustomPersistentDataTypes.PersistentEnumType<>(Particle.class));

    public RegisteredPersistentDataType(String nativeKey, PersistentDataType<?, T> dataType) {
        this(WbsWandcraft.getKey(nativeKey), dataType);
    }
    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
