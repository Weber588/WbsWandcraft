package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang3.NotImplementedException;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.particles.ParticleDataProvider;
import wbs.utils.util.string.WbsStrings;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.cost.PlayerMana;
import wbs.wandcraft.resourcepack.DynamicItemTextureProvider;
import wbs.wandcraft.resourcepack.ResourcePackBuilder;
import wbs.wandcraft.resourcepack.TextureLayer;
import wbs.wandcraft.spell.MagicDomain;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static wbs.wandcraft.util.MenuUtils.DESCRIPTION_STYLE;

@NullMarked
public abstract class SpellDefinition implements ISpellDefinition, DynamicItemTextureProvider, ParticleDataProvider {
    protected final Set<SpellAttributeInstance<?>> defaultAttributes = new HashSet<>();

    protected final List<MagicDomain> magicDomains = new LinkedList<>();

    private final NamespacedKey key;
    protected int echoShardCost = -1;
    @Nullable
    private List<TextureLayer> textureLayers;

    SpellDefinition(String nativeKey) {
        this(WbsWandcraft.getKey(nativeKey));
    }
    public SpellDefinition(NamespacedKey key) {
        this.key = key;
        setupAttributables();
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
                getPrimaryDomain().textColor()
        );
    }

    public MagicDomain getPrimaryDomain() {
        return magicDomains.stream().findFirst().orElseThrow(() -> new IllegalStateException("Spell definition lacked magic domain!"));
    }

    @Nullable
    public MagicDomain getSecondaryDomain() {
        if (magicDomains.size() > 1) {
            return magicDomains.get(1);
        }
        return null;
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
    protected String rawDescription() {
        throw new NotImplementedException("Raw description was not implemented but still called from description()");
    }
    public List<Component> loreDescription() {
        LinkedList<Component> components = new LinkedList<>();
        WbsStrings.wrapText(PlainTextComponentSerializer.plainText().serialize(description()), 140).stream()
                .map(Component::text)
                .map(component -> component.style(DESCRIPTION_STYLE))
                .forEachOrdered(components::add);
        return components;
    }

    @Override
    public final List<TextureLayer> getTextures() {
        if (textureLayers == null) {
            String texture = "spell_" + getTextureKeyValue();

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

    protected String getTextureKeyValue() {
        return key().value();
    }

    public void addMagicDomain(MagicDomain domain) {
        this.magicDomains.add(domain);
    }

    public void registerEvents() {

    }

    public List<MagicDomain> getDomains() {
        return new LinkedList<>(magicDomains);
    }

    public Component getDomainDisplays() {
        return Component.join(
                JoinConfiguration.builder()
                        .separator(Component.text(" - ")
                                .style(DESCRIPTION_STYLE)
                                .decorate(TextDecoration.ITALIC)
                        )
                        .build(),
                getDomains()
                        .stream()
                        .map(domain ->
                                domain.displayName()
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

    public SpellInstance newInstance() {
        return new SpellInstance(this);
    }

    @Override
    public Color getColor(Particle particle, @Nullable Location location) {
        return getPrimaryDomain().color();
    }

    @Override
    public Color getSecondaryColor(Particle particle, @Nullable Location location) {
        MagicDomain secondaryMagicDomain = getSecondaryDomain();
        if (secondaryMagicDomain != null) {
            return secondaryMagicDomain.color();
        }

        return getColor(particle, location);
    }
}
