package wbs.wandcraft.spell.attributes.modifier;

import net.kyori.adventure.text.Component;
import org.bukkit.persistence.PersistentDataType;
import wbs.wandcraft.AttributeDataType;
import wbs.wandcraft.spell.attributes.SpellAttribute;

public abstract class AttributeModificationOperator<T, M> {
    private final AttributeModifierType definition;
    private final PersistentDataType<?, T> baseType;
    private final AttributeDataType<M> modifierType;

    public AttributeModificationOperator(AttributeModifierType definition, PersistentDataType<?, T> baseType, AttributeDataType<M> modifierType) {
        this.definition = definition;
        this.baseType = baseType;
        this.modifierType = modifierType;
    }

    public abstract T modify(T current, M value);

    public abstract Component asComponent(SpellAttribute<T> attribute, M modifierValue);

    public PersistentDataType<?, T> getBaseType() {
        return baseType;
    }

    public AttributeDataType<M> getModifierType() {
        return modifierType;
    }

    public AttributeModifierType getDefinition() {
        return definition;
    }

    public abstract SpellAttribute.Sentiment getSentiment(M modifierValue);
}
