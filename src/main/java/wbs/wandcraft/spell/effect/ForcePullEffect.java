package wbs.wandcraft.spell.effect;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.trigger.SupportedEvent;

import static wbs.wandcraft.spell.attributes.attributable.RangeAttributable.RANGE;
import static wbs.wandcraft.spell.attributes.attributable.SpeedAttributable.SPEED;

@NullMarked
public class ForcePullEffect extends SpellEffectDefinition<Location> {
    public ForcePullEffect() {
        super(Location.class, "force_pull");

        addAttribute(RANGE);
        addAttribute(SPEED);

        supportedEvents.add(SupportedEvent.LOCATION_RAYTRACE);
        supportedEvents.add(SupportedEvent.LOCATION_MAGIC_OBJECT);
    }

    @Override
    public void run(CastContext context, SpellEffectInstance<Location> effectInstance, Location location) {
        RadiusSelector<Entity> selector = new RadiusSelector<>(Entity.class).setRange(effectInstance.getAttribute(RANGE));

        selector.select(location).forEach(entity -> {
            Vector entityToLocation = location.clone().subtract(entity.getLocation()).toVector();

            entity.setVelocity(entity.getVelocity().add(WbsMath.scaleVector(entityToLocation, effectInstance.getAttribute(SPEED))));
        });
    }

    @Override
    public Component toComponent(SpellEffectInstance<Location> instance) {
        return Component.text("Pulls nearby entities.");
    }
}
