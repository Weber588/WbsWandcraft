package wbs.wandcraft.spell.attributes;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.ComponentRepresentable;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;

public final class SpellAttributeInstance<T> implements ComponentRepresentable {
    private final SpellAttribute<T> attribute;
    @NotNull
    private T value;

    public SpellAttributeInstance(SpellAttribute<T> attribute, @NotNull T value) {
        this.attribute = attribute;
        this.value = value;
    }
    public void value(@NotNull Object value) {
        if (this.value.getClass().isInstance(value)) {
            //noinspection unchecked
            this.value = (T) value;
        }
    }

    public SpellAttribute<T> attribute() {
        return attribute;
    }

    public T value() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SpellAttributeInstance<?> other) {
            return other.attribute.equals(this.attribute) &&
                    other.value == this.value;
        }

        return false;
    }

    public void writeTo(PersistentDataContainer attributes) {
        attributes.set(attribute.getKey(), attribute.type(), value());
    }

    public <O> void modify(SpellAttributeModifier<O> modifier) {
        if (modifier.attribute().equals(attribute)) {
            //noinspection unchecked
            value(modifier.modify((O) value()));
        }
    }

    @Override
    public Component toComponent() {
        return attribute().displayName().color(NamedTextColor.GOLD)
                .append(Component.text(": "))
                .append(Component.text(value().toString()).color(NamedTextColor.AQUA));
    }
}
