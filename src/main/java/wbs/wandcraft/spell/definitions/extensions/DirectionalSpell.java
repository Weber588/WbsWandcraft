package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import wbs.utils.util.WbsMath;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

public interface DirectionalSpell extends ISpellDefinition {
    SpellAttribute<Double> IMPRECISION = new DoubleSpellAttribute("imprecision", 15)
            .setShowAttribute(value -> value != 0)
            .setNumericFormatter(accuracy -> accuracy + " degrees")
            .sentiment(SpellAttribute.Sentiment.NEGATIVE);

    default Vector getDirection(CastContext context) {
        if (this instanceof RangedSpell) {
            double range = context.instance().getAttribute(RangedSpell.RANGE);
            return getDirection(context, range);
        }

        return getDirection(context, 1);
    }

    default Vector getDirection(CastContext context, double magnitude) {
        return getDirection(context, context.location(), magnitude);
    }
    default Vector getDirection(CastContext context, Player player, double magnitude) {
        return getDirection(context, player.getEyeLocation(), magnitude);
    }
    default Vector getDirection(CastContext context, Location location, double magnitude) {
        Vector direction = location.getDirection();
        if (direction.lengthSquared() == 0) {
            return WbsMath.scaleVector(direction, magnitude);
        }

        double imprecision = context.instance().getAttribute(IMPRECISION);
        double offsetAngle = Math.random() * imprecision;

        if (offsetAngle <= 0) {
            return WbsMath.scaleVector(direction, magnitude);
        }

        return WbsMath.scaleVector(WbsMath.rotateRandomDirection(direction, offsetAngle), magnitude);
    }

    default void setupDirectional() {
        addAttribute(IMPRECISION);
    }
}
