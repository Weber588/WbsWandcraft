package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.string.WbsStrings;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.resourcepack.TextureLayer;
import wbs.wandcraft.resourcepack.TextureProvider;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.definitions.extensions.SpellExtensionManager;
import wbs.wandcraft.spell.definitions.type.SpellType;
import wbs.wandcraft.spell.event.SpellTriggeredEvent;

import java.util.*;

import static wbs.wandcraft.spellbook.Spellbook.DESCRIPTION_COLOR;

public abstract class SpellDefinition implements ISpellDefinition, TextureProvider {
    protected final Map<Key, SpellTriggeredEvent<?>> events = new HashMap<>();

    protected final Set<SpellAttributeInstance<?>> defaultAttributes = new HashSet<>();

    protected final List<SpellType> spellTypes = new LinkedList<>();

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
        return Component.text(
                WbsStrings.capitalizeAll(key.value().replaceAll("_", " "))
        ).color(
                getPrimarySpellType().textColor()
        );
    }

    public @NotNull SpellType getPrimarySpellType() {
        return spellTypes.stream().findFirst().orElse(SpellType.ARCANE);
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
    public Set<SpellAttributeInstance<?>> getAttributeInstances() {
        return defaultAttributes;
    }

    public Component description() {
        return Component.text(rawDescription());
    }
    public abstract String rawDescription();
    public List<Component> loreDescription() {
        LinkedList<Component> components = new LinkedList<>();
        WbsStrings.wrapText(rawDescription(), 140).stream()
                .map(Component::text)
                .map(component -> component.color(DESCRIPTION_COLOR))
                .forEachOrdered(components::add);
        return components;
    }

    @Override
    public @NotNull final List<TextureLayer> getTextures() {
        return List.of(
                new TextureLayer("spell_" + key().value())
        );
    }

    public void addSpellType(SpellType type) {
        this.spellTypes.add(type);
    }

    public void registerEvents() {

    }

    public List<SpellType> getTypes() {
        return new LinkedList<>(spellTypes);
    }

    public Component getTypesDisplay() {
        return Component.join(
                JoinConfiguration.builder()
                        .separator(Component.text(" - ")
                                .decorate(TextDecoration.ITALIC)
                                .color(DESCRIPTION_COLOR)
                        )
                        .build(),
                getTypes()
                        .stream()
                        .map(spellType ->
                                spellType.displayName()
                                        .color(spellType.textColor())
                                        .decorate(TextDecoration.ITALIC)
                        )
                        .toList()
        );
    }
}
