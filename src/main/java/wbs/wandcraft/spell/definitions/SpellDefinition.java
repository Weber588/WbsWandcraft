package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.event.SpellTriggeredEvent;
import wbs.wandcraft.spell.definitions.extensions.SpellExtensionManager;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class SpellDefinition implements AbstractSpellDefinition {
    protected final Map<Key, SpellAttribute<?>> attributes = new HashMap<>();
    protected final Map<Key, SpellTriggeredEvent<?>> events = new HashMap<>();

    private final NamespacedKey key;

    SpellDefinition(String nativeKey) {
        this(WbsWandcraft.getKey(nativeKey));
    }
    public SpellDefinition(NamespacedKey key) {
        this.key = key;
        SpellExtensionManager.setup(this);
    }

    public void addAttribute(SpellAttribute<?> attribute) {
        attributes.put(attribute.key(), attribute);
    }

    @Nullable
    public <T> SpellAttribute<T> getAttribute(Key key, Class<T> clazz) {
        SpellAttribute<?> attribute = attributes.get(key);

        if (clazz.isInstance(attribute.defaultValue())) {
            //noinspection unchecked
            return (SpellAttribute<T>) attribute;
        }

        return null;
    }

    public Collection<SpellAttribute<?>> getAttributes() {
        return attributes.values();
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }
}
