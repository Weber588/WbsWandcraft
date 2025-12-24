package wbs.wandcraft.wand.types;

import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.persistence.PersistentDataContainerView;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.util.persistent.AbstractPersistentWandType;
import wbs.wandcraft.util.persistent.CustomPersistentDataTypes;
import wbs.wandcraft.wand.Wand;
import wbs.wandcraft.wand.WandTexture;

import java.util.UUID;
import java.util.function.Supplier;

public class WandType<T extends Wand> implements Keyed {
    public static final WandType<BasicWand> BASIC = new WandType<>(
            WbsWandcraft.getKey("basic"),
            CustomPersistentDataTypes.BASIC_WAND_TYPE,
            WandTexture.BASIC,
            ItemUseAnimation.BLOCK,
            15,
            () -> new BasicWand(UUID.randomUUID().toString())
    );
    public static final WandType<WizardryWand> WIZARDRY = new WandType<>(
            WbsWandcraft.getKey("wizardry"),
            CustomPersistentDataTypes.WIZARDRY_WAND_TYPE,
            WandTexture.WIZARDRY,
            ItemUseAnimation.BOW,
            10,
            () -> new WizardryWand(UUID.randomUUID().toString())
    );
    public static final WandType<SorceryWand> SORCERY = new WandType<>(
            WbsWandcraft.getKey("sorcery"),
            CustomPersistentDataTypes.SORCERY_WAND_TYPE,
            WandTexture.SORCERY,
            null,
            0,
            () -> new SorceryWand(UUID.randomUUID().toString())
    );
    public static final WandType<MageWand> MAGE = new WandType<>(
            WbsWandcraft.getKey("mage"),
            CustomPersistentDataTypes.MAGE_WAND_TYPE,
            WandTexture.MAGE,
            null,
            0,
            () -> new MageWand(UUID.randomUUID().toString())
    );

    private final NamespacedKey key;
    private final AbstractPersistentWandType<T> persistentDataType;
    private final WandTexture wandTexture;
    @Nullable
    private final ItemUseAnimation animation;
    private final int animationTicks;
    private final Supplier<T> supplier;

    public WandType(NamespacedKey key, AbstractPersistentWandType<T> persistentDataType, WandTexture wandTexture, ItemUseAnimation animation, int animationTicks, Supplier<T> supplier) {
        this.key = key;
        this.persistentDataType = persistentDataType;
        this.wandTexture = wandTexture;
        this.animation = animation;
        this.animationTicks = animationTicks;
        this.supplier = supplier;
    }

    public T getWand(PersistentDataContainerView container) {
        return container.get(Wand.WAND_KEY, persistentDataType);
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public T newWand() {
        return supplier.get();
    }

    public WandTexture getWandTexture() {
        return wandTexture;
    }

    public @Nullable ItemUseAnimation getAnimation() {
        return animation;
    }

    public int getAnimationTicks() {
        return animationTicks;
    }
}
