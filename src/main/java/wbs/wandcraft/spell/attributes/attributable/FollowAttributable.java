package wbs.wandcraft.spell.attributes.attributable;

import wbs.wandcraft.spell.attributes.AttributeHolder;
import wbs.wandcraft.spell.attributes.BooleanSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;

public interface FollowAttributable extends AttributeHolder {
    SpellAttribute<Boolean> FOLLOWS_PLAYER = new BooleanSpellAttribute("follow_player", false);

    @AttributableSetupHandler
    default void setUpFollowing() {
        addAttribute(FOLLOWS_PLAYER);
    }
}
