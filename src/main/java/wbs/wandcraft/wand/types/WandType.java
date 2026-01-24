package wbs.wandcraft.wand.types;

import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastingQueue;
import wbs.wandcraft.util.persistent.AbstractPersistentWandType;
import wbs.wandcraft.util.persistent.CustomPersistentDataTypes;
import wbs.wandcraft.wand.Wand;
import wbs.wandcraft.wand.WandTexture;

import java.util.UUID;
import java.util.function.Supplier;

public class WandType<T extends Wand> implements Keyed {
    // TODO: Add flavour text
    public static final WandType<BasicWand> BASIC = new WandType<>(
            WbsWandcraft.getKey("basic"),
            Component.text("Basic Wand"),
            "A wand with a single slot for a spell scroll.",
            1,
            CustomPersistentDataTypes.BASIC_WAND_TYPE,
            WandTexture.BASIC,
            ItemUseAnimation.BLOCK,
            CastingQueue.DEFAULT_CAST_DELAY - 1,
            () -> new BasicWand(UUID.randomUUID().toString()));
    public static final WandType<MageWand> MAGE = new WandType<>(
            WbsWandcraft.getKey("mage"),
            Component.text("Mage Wand").color(TextColor.color(0x72159e)),
            "A wand with a list of slots that can be cycled between, allowing you to choose what to cast.",
            16,
            CustomPersistentDataTypes.MAGE_WAND_TYPE,
            WandTexture.MAGE,
            null,
            0,
            () -> new MageWand(UUID.randomUUID().toString()));
    public static final WandType<WizardryWand> WIZARDRY = new WandType<>(
            WbsWandcraft.getKey("wizardry"),
            Component.text("Wizardry Wand").color(TextColor.color(0x15859e)),
            "A wand with a list of slots that ALL cast rapidly when the wand is used.",
            32,
            CustomPersistentDataTypes.WIZARDRY_WAND_TYPE,
            WandTexture.WIZARDRY,
            ItemUseAnimation.BOW,
            10,
            () -> new WizardryWand(UUID.randomUUID().toString()));
    public static final WandType<SorceryWand> SORCERY = new WandType<>(
            WbsWandcraft.getKey("sorcery"),
            Component.text("Sorcery Wand").color(TextColor.color(0x173B08)),
            "A wand with a slot for various controls, allowing a different spell for punch, right click, and a few others.",
            32,
            CustomPersistentDataTypes.SORCERY_WAND_TYPE,
            WandTexture.SORCERY,
            null,
            0,
            () -> new SorceryWand(UUID.randomUUID().toString()));
    public static final WandType<WildenWand> WILDEN = new WandType<>(
            WbsWandcraft.getKey("wilden"),
            Component.text("Wilden Wand").color(TextColor.color(0x409e15)),
            "A wand with a list of slots from which a random spell is cast, with cooldown from a random spell in the wand.",
            12,
            CustomPersistentDataTypes.WILDEN_WAND_TYPE,
            WandTexture.WILDEN,
            null,
            0,
            () -> new WildenWand(UUID.randomUUID().toString()));
    public static final WandType<BarbarianWand> BARBARIAN = new WandType<>(
            WbsWandcraft.getKey("barbarian"),
            Component.text("Barbarian Wand").color(TextColor.color(0x9e2e15)),
            "A wand with a single slot, with cooldown halved, but only works at melee range.",
            12,
            CustomPersistentDataTypes.BARBARIAN_WAND_TYPE,
            WandTexture.BARBARIAN,
            null,
            0,
            () -> new BarbarianWand(UUID.randomUUID().toString()));

    private final NamespacedKey key;
    private final @NotNull Component itemName;
    private final @NotNull String rawDescription;
    private final int echoShardCost;
    private final AbstractPersistentWandType<T> persistentDataType;
    private final WandTexture wandTexture;
    @Nullable
    private final ItemUseAnimation animation;
    private final int animationTicks;
    private final Supplier<T> supplier;

    public WandType(NamespacedKey key, @NotNull Component itemName, @NotNull String rawDescription, int echoShardCost, AbstractPersistentWandType<T> persistentDataType, WandTexture wandTexture, ItemUseAnimation animation, int animationTicks, Supplier<T> supplier) {
        this.key = key;
        this.itemName = itemName;
        this.rawDescription = rawDescription;
        this.echoShardCost = echoShardCost;
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

    public @NotNull Component getItemName() {
        return itemName;
    }

    public @NotNull String getRawDescription() {
        return rawDescription;
    }

    public @NotNull Component getDescription() {
        return Component.text(rawDescription);
    }

    public int getEchoShardCost() {
        return echoShardCost;
    }
}
