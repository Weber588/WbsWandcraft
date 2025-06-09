package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.util.Vector;
import wbs.utils.util.WbsMath;
import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

public interface DirectionalSpell extends ISpellDefinition {
    SpellAttribute<Double> IMPRECISION = new DoubleSpellAttribute("imprecision", 15)
            .setNumericFormatter(accuracy -> accuracy + " degrees");

    default Vector getDirection(CastContext context) {
        return getDirection(context, 1);
    }

    default Vector getDirection(CastContext context, double magnitude) {
        Vector direction = context.location().getDirection();
        if (direction.lengthSquared() == 0) {
            return direction;
        }

        double imprecision = context.instance().getAttribute(IMPRECISION);
        double offsetAngle = Math.random() * imprecision;

        if (offsetAngle <= 0) {
            return direction;
        }

        return WbsMath.scaleVector(WbsMath.rotateRandomDirection(direction, offsetAngle), magnitude);
    }

    default void setupDirectional() {
        addAttribute(IMPRECISION);
    }
}
