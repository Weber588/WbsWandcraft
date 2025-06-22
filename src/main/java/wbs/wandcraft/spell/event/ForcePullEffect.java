package wbs.wandcraft.spell.event;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.wandcraft.context.CastContext;

import static wbs.wandcraft.spell.definitions.extensions.RangedSpell.RANGE;
import static wbs.wandcraft.spell.definitions.extensions.SpeedSpell.SPEED;

@NullMarked
public class ForcePullEffect extends SpellEffectDefinition<Location> {
    public ForcePullEffect() {
        super(Location.class, "force_pull");

        setAttribute(RANGE.defaultInstance());
        setAttribute(SPEED.defaultInstance());

        supportedEvents.add(SupportedEvent.LOCATION_RAYTRACE);
    }

    @Override
    public void run(CastContext context, SpellEffectInstance<Location> effectInstance, Location event) {
        Location location = context.location();

        RadiusSelector<Entity> selector = new RadiusSelector<>(Entity.class).setRange(effectInstance.getAttribute(RANGE));

        selector.select(location).forEach(entity -> {
            Vector entityToLocation = location.clone().subtract(entity.getLocation()).toVector();

            entity.setVelocity(entity.getVelocity().add(WbsMath.scaleVector(entityToLocation, effectInstance.getAttribute(SPEED))));
        });
    }

    @Override
    public Component toComponent() {
        return Component.text("Pulls all entities and magic objects towards the location.");
    }
}
