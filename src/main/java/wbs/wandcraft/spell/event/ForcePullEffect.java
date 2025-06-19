package wbs.wandcraft.spell.event;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.extensions.CastContext;

import java.util.Objects;

@NullMarked
public class ForcePullEffect extends SpellEffectDefinition<Location> {
    private static final SpellAttribute<Double> RANGE = new DoubleSpellAttribute("range",  5);
    private static final SpellAttribute<Double> SPEED = new DoubleSpellAttribute("speed", 1);

    public ForcePullEffect() {
        super(Location.class, "force_pull");

        setAttribute(RANGE.defaultInstance());
        setAttribute(SPEED.defaultInstance());

        supportedEvents.add(new SupportedEvent<>(RayTraceResult.class, result -> {
            World world = null;

            Entity hitEntity = result.getHitEntity();
            if (hitEntity != null) {
                world = hitEntity.getWorld();
            }

            Block hitBlock = result.getHitBlock();
            if (hitBlock != null) {
                world = hitBlock.getWorld();
            }

            return result.getHitPosition().toLocation(Objects.requireNonNull(world));
        }));
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
