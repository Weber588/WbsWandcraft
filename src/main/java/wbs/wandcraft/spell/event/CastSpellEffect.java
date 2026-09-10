package wbs.wandcraft.spell.event;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.RegisteredPersistentDataType;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;

@NullMarked
public class CastSpellEffect extends SpellEffectDefinition<Location> {
    private static final SpellAttribute<SpellInstance> SPELL = new SpellAttribute<>(
            "spell",
            RegisteredPersistentDataType.SPELL,
            new SpellInstance(WandcraftRegistries.SPELLS.stream().findAny().orElseThrow()),
            string -> {
                SpellDefinition definition = WandcraftRegistries.SPELLS.get(NamespacedKey.fromString(string, WbsWandcraft.getInstance()));
                if (definition == null) {
                    //noinspection DataFlowIssue
                    return null;
                }
                return new SpellInstance(definition);
            }).addRawSuggestions(WandcraftRegistries.SPELLS.stream().map(SpellDefinition::key).map(Key::asString).toList());

    public CastSpellEffect() {
        super(Location.class, "cast_spell");

        setAttribute(SPELL.defaultInstance());

        supportedEvents.add(SupportedEvent.LOCATION_RAYTRACE);
    }

    @Override
    public void run(CastContext context, SpellEffectInstance<Location> instance, Location event) {
        SpellInstance triggeredCast = instance.getAttribute(SPELL);

        CastContext updatedContext = new CastContext(context.player(), triggeredCast, context.wand(), context.slot(), event, context, null);
        triggeredCast.cast(updatedContext);
    }

    @Override
    public Component toComponent(SpellEffectInstance<Location> instance) {
        return Component.text("Cast ").append(instance.getAttribute(SPELL).getDefinition().displayName());
    }
}
