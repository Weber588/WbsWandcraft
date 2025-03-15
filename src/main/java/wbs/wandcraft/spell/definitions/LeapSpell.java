package wbs.wandcraft.spell.definitions;

import wbs.utils.util.entities.WbsEntityUtil;
import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.extensions.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;

public class LeapSpell extends SpellDefinition implements CastableSpell {
    private static final SpellAttribute<Double> SPEED = new DoubleSpellAttribute("speed", 0, 3);

    public LeapSpell() {
        super("leap");
        addAttribute(SPEED);
    }

    @Override
    public void cast(CastContext context) {
        WbsEntityUtil.push(context.player(), context.instance().getAttribute(SPEED));
    }
}
