package wbs.wandcraft.spell.definitions;

import wbs.utils.util.entities.WbsEntityUtil;
import wbs.wandcraft.spell.definitions.extensions.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.SpeedSpell;

public class LeapSpell extends SpellDefinition implements CastableSpell, SpeedSpell {
    public LeapSpell() {
        super("leap");
    }

    @Override
    public void cast(CastContext context) {
        WbsEntityUtil.push(context.player(), context.instance().getAttribute(SPEED));
    }
}
