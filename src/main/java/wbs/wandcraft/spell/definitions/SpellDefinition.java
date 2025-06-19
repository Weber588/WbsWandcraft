package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.string.WbsStrings;
import wbs.wandcraft.TextureProvider;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.definitions.extensions.SpellExtensionManager;
import wbs.wandcraft.spell.event.SpellTriggeredEvent;

import java.util.*;

public abstract class SpellDefinition implements ISpellDefinition, TextureProvider {
    protected final Map<Key, SpellTriggeredEvent<?>> events = new HashMap<>();

    protected final Set<SpellAttributeInstance<?>> defaultAttributes = new HashSet<>();

    private final NamespacedKey key;

    SpellDefinition(String nativeKey) {
        this(WbsWandcraft.getKey(nativeKey));
    }
    public SpellDefinition(NamespacedKey key) {
        this.key = key;
        SpellExtensionManager.setup(this);
    }

    public void addAttribute(SpellAttribute<?> attribute) {
        defaultAttributes.add(attribute.defaultInstance());
    }

    public Collection<SpellAttribute<?>> getAttributes() {
        return new LinkedList<>(defaultAttributes.stream().map(SpellAttributeInstance::attribute).toList());
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    @Override
    public Component displayName() {
        return Component.text(WbsStrings.capitalizeAll(key.value().replaceAll("_", " ")));
    }

    public <T> T getDefault(SpellAttribute<T> attribute) {
        for (SpellAttributeInstance<?> instance : defaultAttributes) {
            if (instance.attribute().equals(attribute)) {
                //noinspection unchecked
                return (T) instance.value();
            }
        }
        return attribute.defaultValue();
    }

    @Override
    public Set<SpellAttributeInstance<?>> getAttributeValues() {
        return defaultAttributes;
    }

    public abstract Component description();

    @Override
    public @NotNull final String getTexture() {
        return "spell_" + key().value();
    }

    public void registerEvents() {

    }
}
