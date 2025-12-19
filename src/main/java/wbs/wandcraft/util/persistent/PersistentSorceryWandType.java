package wbs.wandcraft.util.persistent;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsEnums;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.wand.types.SorceryWand;

import java.util.HashMap;
import java.util.Map;

public class PersistentSorceryWandType extends AbstractPersistentWandType<SorceryWand> {
    private static final NamespacedKey CONTROLS = WbsWandcraft.getKey("controls");
    private static final NamespacedKey TIER = WbsWandcraft.getKey("tier");

    @Override
    public @NotNull Class<SorceryWand> getComplexType() {
        return SorceryWand.class;
    }

    @Override
    protected void writeTo(PersistentDataContainer container, SorceryWand wand, @NotNull PersistentDataAdapterContext context) {
        PersistentDataContainer controlsContainer = context.newPersistentDataContainer();
        wand.getAllItems().forEach((tier, controls) -> {
            PersistentDataContainer tierContainer = controlsContainer.getAdapterContext().newPersistentDataContainer();

            controls.forEach((control, item) -> {
                if (item != null) {
                    tierContainer.set(WbsWandcraft.getKey(control.name().toLowerCase()), WbsPersistentDataType.ITEM_AS_BYTES, item);
                }
            });
            controlsContainer.set(WbsWandcraft.getKey("tier_" + tier), PersistentDataType.TAG_CONTAINER, tierContainer);
        });

        container.set(TIER, PersistentDataType.INTEGER, wand.getTier());
        container.set(CONTROLS, PersistentDataType.TAG_CONTAINER, controlsContainer);
    }

    @Override
    protected @NotNull SorceryWand getWand(@NotNull PersistentDataContainer container, @NotNull String uuid) {
        int tier = container.getOrDefault(TIER, PersistentDataType.INTEGER, 0);
        return new SorceryWand(uuid, tier);
    }

    @Override
    protected void populateWand(SorceryWand wand, @NotNull PersistentDataContainer container) {
        PersistentDataContainer controlsContainer = container.get(CONTROLS, PersistentDataType.TAG_CONTAINER);

        if (controlsContainer == null) {
            throw new IllegalStateException("Missing controls section!");
        }

        Map<Integer, Map<SorceryWand.WandControl, ItemStack>> tiers = new HashMap<>();

        controlsContainer.getKeys().forEach(tierKey -> {
            PersistentDataContainer tierContainer = controlsContainer.get(tierKey, PersistentDataType.TAG_CONTAINER);

            if (tierContainer != null) {
                int tier = getTier(tierKey);
                Map<SorceryWand.WandControl, ItemStack> items = new HashMap<>();

                tierContainer.getKeys().forEach(key -> {
                    SorceryWand.WandControl control = WbsEnums.getEnumFromString(SorceryWand.WandControl.class, key.value());
                    if (control == null) {
                        throw new IllegalStateException("Invalid control: " + key);
                    }

                    ItemStack item = tierContainer.get(key, WbsPersistentDataType.ITEM_AS_BYTES);

                    items.put(control, item);
                });

                tiers.put(tier, items);
            }
        });

        if (!tiers.isEmpty()) {
            wand.setAllItems(tiers);
        }
    }

    private int getTier(NamespacedKey tierKey) {
        return Integer.parseInt(tierKey.value().split("_")[1]);
    }
}
