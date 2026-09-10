package wbs.wandcraft.util.persistent;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.spell.modifier.SpellModifier;

import java.util.LinkedList;
import java.util.List;

public class PersistentSpellModifierType implements WbsPersistentDataType<PersistentDataContainer, SpellModifier> {
    private static final NamespacedKey ATTRIBUTE_MODIFIERS = WbsWandcraft.getKey("attribute_modifiers");
    private static final NamespacedKey EFFECTS = WbsWandcraft.getKey("effects");

    @Override
    public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
        return PersistentDataContainer.class;
    }

    @Override
    public @NotNull Class<SpellModifier> getComplexType() {
        return SpellModifier.class;
    }

    @Override
    public @NotNull PersistentDataContainer toPrimitive(@NotNull SpellModifier modifier, @NotNull PersistentDataAdapterContext context) {
        PersistentDataContainer container = context.newPersistentDataContainer();
        List<PersistentDataContainer> modifierContainerList = new LinkedList<>();
        for (SpellAttributeModifier<?, ?> attributeModifier : modifier.getModifiers()) {
            PersistentDataContainer modifierContainer = context.newPersistentDataContainer();
            attributeModifier.writeTo(modifierContainer);
            modifierContainerList.add(modifierContainer);
        }
        container.set(ATTRIBUTE_MODIFIERS, PersistentDataType.LIST.dataContainers(), modifierContainerList);

        container.set(
                EFFECTS,
                CustomPersistentDataTypes.SPELL_EFFECT.asList(),
                modifier.getEffects()
        );

        return container;
    }

    @Override
    public @NotNull SpellModifier fromPrimitive(@NotNull PersistentDataContainer container, @NotNull PersistentDataAdapterContext context) {
        SpellModifier spellModifier = new SpellModifier();

        List<PersistentDataContainer> modifierContainerList = container.get(ATTRIBUTE_MODIFIERS, PersistentDataType.LIST.dataContainers());
        if (modifierContainerList == null) {
            throw new IllegalStateException("Deserialization of " + ATTRIBUTE_MODIFIERS.asString() + " missing!");
        }

        for (PersistentDataContainer modifierContainer : modifierContainerList) {
            SpellAttributeModifier<?, ?> attributeModifier = SpellAttributeModifier.fromContainer(modifierContainer);

            spellModifier.addModifier(attributeModifier);
        }

        container.getOrDefault(
                EFFECTS,
                CustomPersistentDataTypes.SPELL_EFFECT.asList(),
                List.of()
        ).forEach(spellModifier::addEffect);

        return spellModifier;
    }
}
