package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.util.Vector;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.AbstractSpellDefinition;

public interface DirectionalSpell extends AbstractSpellDefinition {
    SpellAttribute<Double> ACCURACY = new DoubleSpellAttribute("accuracy", 0, 100, 50);

    default double getMaxAngle() {
        return 45;
    }

    default Vector getDirection(CastContext context) {
        Vector direction = WbsEntityUtil.getFacingVector(context.player());
        if (direction.lengthSquared() == 0) {
            return direction;
        }

        double accuracy = context.instance().getAttribute(ACCURACY) / 100;
        double offsetAngle = Math.random() * (1 - accuracy) * getMaxAngle();

        if (offsetAngle <= 0) {
            return direction;
        }

        return WbsMath.rotateRandomDirection(direction, offsetAngle);
    }

    default void setupDirectional() {
        addAttribute(ACCURACY);
    }
}
