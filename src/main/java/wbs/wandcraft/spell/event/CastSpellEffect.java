package wbs.wandcraft.spell.event;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.RegisteredPersistentDataType;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.context.CastContext;

@NullMarked
public class CastSpellEffect extends SpellEffectDefinition<Location> {
    private static final SpellAttribute<SpellInstance> SPELL = new SpellAttribute<>(
            "spell",
            RegisteredPersistentDataType.SPELL,
            new SpellInstance(WandcraftRegistries.SPELLS.stream().findAny().orElseThrow()),
            string -> {
                SpellDefinition defaultDef = WandcraftRegistries.SPELLS.get(NamespacedKey.fromString(string, WbsWandcraft.getInstance()));
                if (defaultDef == null) {
                    throw new IllegalArgumentException("Invalid spell: " + string);
                }
                return new SpellInstance(defaultDef);
            });

    public CastSpellEffect() {
        super(Location.class, "cast_spell");

        setAttribute(SPELL.defaultInstance());

        supportedEvents.add(SupportedEvent.LOCATION_RAYTRACE);
    }

    @Override
    public void run(CastContext context, SpellEffectInstance<Location> effectInstance, Location event) {
        SpellInstance triggeredCast = context.instance().getAttribute(SPELL);

        CastContext updatedContext = new CastContext(context.player(), triggeredCast, event, context, null);
        triggeredCast.cast(updatedContext);
    }

    @Override
    public Component toComponent() {
        return Component.text("Casts a different spell when triggered!");
    }
}
