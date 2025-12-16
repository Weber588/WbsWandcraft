package wbs.wandcraft.util.persistent;


import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.wand.Wand;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public abstract class AbstractPersistentWandType<T extends Wand> implements PersistentDataType<PersistentDataContainer, T> {
    private static final NamespacedKey WAND_ATTRIBUTES = WbsWandcraft.getKey("wand_attributes");
    private static final NamespacedKey WAND_ATTRIBUTE_MODIFIERS = WbsWandcraft.getKey("wand_attribute_modifiers");
    private static final NamespacedKey UUID = WbsWandcraft.getKey("wand_uuid");

    public static final NamespacedKey WAND_TYPE = WbsWandcraft.getKey("wand_type");

    @Override
    public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
        return PersistentDataContainer.class;
    }

    @Override
    public final @NotNull PersistentDataContainer toPrimitive(@NotNull T wand, @NotNull PersistentDataAdapterContext context) {
        PersistentDataContainer container = context.newPersistentDataContainer();

        wand.writeAttributes(container, WAND_ATTRIBUTES);

        List<PersistentDataContainer> modifierContainerList = new LinkedList<>();
        for (SpellAttributeModifier<?, ?> attributeModifier : wand.getAttributeModifiers()) {
            PersistentDataContainer modifierContainer = context.newPersistentDataContainer();
            attributeModifier.writeTo(modifierContainer);
            modifierContainerList.add(modifierContainer);
        }
        container.set(WAND_ATTRIBUTE_MODIFIERS, PersistentDataType.LIST.dataContainers(), modifierContainerList);
        container.set(UUID, PersistentDataType.STRING, wand.getUUID());

        writeTo(container, wand, context);

        return container;
    }

    protected abstract void writeTo(PersistentDataContainer container, T wand, @NotNull PersistentDataAdapterContext context);

    @Override
    public final @NotNull T fromPrimitive(@NotNull PersistentDataContainer container, @NotNull PersistentDataAdapterContext context) {
        String uuid = container.get(UUID, PersistentDataType.STRING);

        T wand = getWand(container, Objects.requireNonNull(uuid));

        wand.readAttributes(container, WAND_ATTRIBUTES);

        List<PersistentDataContainer> modifierContainerList = container.get(WAND_ATTRIBUTE_MODIFIERS, PersistentDataType.LIST.dataContainers());
        if (modifierContainerList != null) {
            for (PersistentDataContainer modifierContainer : modifierContainerList) {
                SpellAttributeModifier<?, ?> attributeModifier = SpellAttributeModifier.fromContainer(modifierContainer);

                wand.setModifier(attributeModifier);
            }
        }

        populateWand(wand, container);

        return wand;
    }

    protected abstract void populateWand(T wand, @NotNull PersistentDataContainer container);

    protected abstract @NotNull T getWand(@NotNull PersistentDataContainer type, String uuid);
}