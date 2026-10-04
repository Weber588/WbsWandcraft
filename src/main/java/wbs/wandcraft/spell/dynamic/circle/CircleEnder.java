package wbs.wandcraft.spell.dynamic.circle;

import io.papermc.paper.entity.TeleportFlag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.objects.MagicObjectManager;
import wbs.wandcraft.objects.generics.MagicObject;
import wbs.wandcraft.spell.SpellType;
import wbs.wandcraft.spell.dynamic.DynamicSpellMagicCircle;
import wbs.wandcraft.spell.trigger.SpellTriggeredEvents;
import wbs.wandcraft.util.EffectUtils;

import java.util.Comparator;
import java.util.List;

@NullMarked
public class CircleEnder extends DynamicSpellMagicCircle {
    public static final org.bukkit.NamespacedKey ENDER_CIRCLE_TP_TAG = WbsWandcraft.getKey("ender_circle_tp");

    public CircleEnder(@Nullable SpellType secondary) {
        super(secondary);

        setAttribute(DURATION, 15 * 60 * Ticks.TICKS_PER_SECOND);
    }

    @Override
    public void cast(CastContext context) {
        super.cast(context);

        context.player().getPersistentDataContainer().set(ENDER_CIRCLE_TP_TAG, PersistentDataType.INTEGER, Bukkit.getCurrentTick());

        SpellTriggeredEvents.INDIRECT_TARGET_ENTITY_TRIGGER.getAnonymousInstance(((_, _, entity) -> {
            int lastTPTick = entity.getPersistentDataContainer().getOrDefault(ENDER_CIRCLE_TP_TAG, PersistentDataType.INTEGER, 0);

            // If entity has been out of the circle for less than 0.5 seconds (or never left), update the tag and stop.
            // This way the value is always current until they're outside for long enough.
            if (Bukkit.getCurrentTick() - lastTPTick < 0.5 * Ticks.TICKS_PER_SECOND) {
                entity.getPersistentDataContainer().set(ENDER_CIRCLE_TP_TAG, PersistentDataType.INTEGER, Bukkit.getCurrentTick());
                return;
            }

            List<MagicCircleObject> sortedEnderCircles = MagicObjectManager.getAllActive(MagicCircleObject.class)
                    .stream()
                    .filter(activeCircle ->
                            activeCircle.getContext().instance().getDefinition().getSecondarySpellType() == SpellType.ENDER
                    ).sorted(Comparator.comparing(
                            MagicObject::getLocation,
                            Comparator.comparingDouble(loc -> loc.distance(entity.getLocation()))
                    )).toList();

            if (sortedEnderCircles.size() > 1) {
                MagicCircleObject target = sortedEnderCircles.get(1);
                Location targetLoc = target.getLocation();

                EffectUtils.playTeleportEffect(entity.getLocation());
                Vector direction = entity.getLocation().getDirection();
                entity.teleport(targetLoc.setDirection(direction), TeleportFlag.Relative.VELOCITY_ROTATION);
                EffectUtils.playTeleportEffect(targetLoc);

                entity.getPersistentDataContainer().set(
                        ENDER_CIRCLE_TP_TAG,
                        PersistentDataType.INTEGER,
                        Bukkit.getCurrentTick()
                );
            }
        }));
    }

    @Override
    public Component displayName() {
        return super.displayName();
    }

    @Override
    public boolean requiresConcentration() {
        return false;
    }
}
