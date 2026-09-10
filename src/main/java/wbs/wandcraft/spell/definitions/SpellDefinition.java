package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.string.WbsStrings;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.cost.PlayerMana;
import wbs.wandcraft.resourcepack.ResourcePackBuilder;
import wbs.wandcraft.resourcepack.TextureLayer;
import wbs.wandcraft.resourcepack.DynamicItemTextureProvider;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.SpellExtensionManager;
import wbs.wandcraft.spell.definitions.type.SpellType;
import wbs.wandcraft.spell.event.SpellTriggeredEvent;

import java.util.*;

import static wbs.wandcraft.spellbook.Spellbook.DESCRIPTION_COLOR;

@NullMarked
public abstract class SpellDefinition implements ISpellDefinition, DynamicItemTextureProvider {
    protected final Map<Key, SpellTriggeredEvent<?>> events = new HashMap<>();

    protected final Set<SpellAttributeInstance<?>> defaultAttributes = new HashSet<>();

    protected final List<SpellType> spellTypes = new LinkedList<>();

    private final NamespacedKey key;
    protected int echoShardCost = -1;
    @Nullable
    private List<TextureLayer> textureLayers;

    SpellDefinition(String nativeKey) {
        this(WbsWandcraft.getKey(nativeKey));
    }
    public SpellDefinition(NamespacedKey key) {
        this.key = key;
        SpellExtensionManager.setup(this);
    }

    public void addAttribute(SpellAttribute<?> attribute) {
        SpellAttributeInstance<?> instance = attribute.defaultInstance();
        if (instance == null) {
            WbsWandcraft.getInstance().debug(SpellAttribute.DEBUG_CHANNEL_ATTRIBUTES, "Null default instance passed to SpellDefinition#addAttribute");
        } else {
            defaultAttributes.add(instance);
        }
    }

    public Collection<SpellAttribute<?>> getAttributes() {
        return new LinkedList<>(defaultAttributes.stream().map(SpellAttributeInstance::attribute).toList());
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }

    @Override
    public Component displayName() {
        return Component.text(
                WbsStrings.capitalizeAll(key.value().replace("_", " "))
        ).color(
                getPrimarySpellType().textColor()
        );
    }

    public SpellType getPrimarySpellType() {
        return spellTypes.stream().findFirst().orElse(SpellType.ARCANE);
    }

    @UnknownNullability
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
    public final List<TextureLayer> getTextures() {
        if (textureLayers == null) {
            String texture = "spell_" + key().value();

            String path = ResourcePackBuilder.getTexturesFolder(ResourcePackBuilder.WANDCRAFT, "item") + texture + ".png";
            WbsWandcraft plugin = WbsWandcraft.getInstance();
            if (plugin.getResource(path) == null) {
                plugin.debug(
                        ResourcePackBuilder.DEBUG_RESOURCE_PACK,
                        "The resource at path \"" + path + "\" was not found! A default texture will be used."
                );

                textureLayers = List.of(
                        new TextureLayer("default_spell_text_overlay").defaultTint(0x008000),
                        new TextureLayer("default_spell_background")
                );
            } else {
                textureLayers = List.of(
                        new TextureLayer(texture)
                );
            }
        }

        return textureLayers;
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

    public SpellDefinition setEchoShardCost(int echoShardCost) {
        this.echoShardCost = echoShardCost;
        return this;
    }

    public int getEchoShardCost() {
        if (echoShardCost < 0) {
            return Math.max(1, 64 * getDefault(CastableSpell.COST) / PlayerMana.DEFAULT_MAX_MANA);
        }

        return echoShardCost;
    }
}
