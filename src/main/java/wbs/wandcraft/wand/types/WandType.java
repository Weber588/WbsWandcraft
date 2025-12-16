package wbs.wandcraft.wand.types;

import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.util.persistent.AbstractPersistentWandType;
import wbs.wandcraft.util.persistent.CustomPersistentDataTypes;
import wbs.wandcraft.wand.Wand;

import java.util.UUID;
import java.util.function.Supplier;

public class WandType<T extends Wand> implements Keyed {
    public static final WandType<WizardryWand> WIZARDRY = new WandType<>(
            WbsWandcraft.getKey("wizardry"),
            CustomPersistentDataTypes.WIZARDRY_WAND_TYPE,
            () -> new WizardryWand(UUID.randomUUID().toString())
    );
    public static final WandType<WizardryWand> SORCERY = new WandType<>(
            WbsWandcraft.getKey("sorcery"),
            CustomPersistentDataTypes.WIZARDRY_WAND_TYPE, // TODO
            () -> new WizardryWand(UUID.randomUUID().toString()) // TODO
    );

    private final NamespacedKey key;
    private final AbstractPersistentWandType<T> persistentDataType;
    private final Supplier<T> supplier;

    public WandType(NamespacedKey key, AbstractPersistentWandType<T> persistentDataType, Supplier<T> supplier) {
        this.key = key;
        this.persistentDataType = persistentDataType;
        this.supplier = supplier;
    }

    public T getWand(PersistentDataContainerView container) {
        return container.get(Wand.WAND_KEY, persistentDataType);
    }

    public void toContainer(T wand, PersistentDataContainer container) {
        container.set(Wand.WAND_KEY, persistentDataType, wand);
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public T newWand() {
        return supplier.get();
    }
}
