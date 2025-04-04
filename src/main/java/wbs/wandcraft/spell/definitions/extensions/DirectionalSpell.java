package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.util.Vector;
import wbs.utils.util.WbsMath;
import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

public interface DirectionalSpell extends ISpellDefinition {
    SpellAttribute<Double> ACCURACY = new DoubleSpellAttribute("accuracy", 0, 100, 75)
            .setFormatter(accuracy -> accuracy + "%");

    default double getMaxAngle() {
        return 25;
    }

    default Vector getDirection(CastContext context) {
        return getDirection(context, 1);
    }

    default Vector getDirection(CastContext context, double magnitude) {
        Vector direction = context.location().getDirection();
        if (direction.lengthSquared() == 0) {
            return direction;
        }

        double accuracy = context.instance().getAttribute(ACCURACY) / 100;
        double offsetAngle = Math.random() * (1 - accuracy) * getMaxAngle();

        if (offsetAngle <= 0) {
            return direction;
        }

        return WbsMath.scaleVector(WbsMath.rotateRandomDirection(direction, offsetAngle), magnitude);
    }

    default void setupDirectional() {
        addAttribute(ACCURACY);
    }
}
