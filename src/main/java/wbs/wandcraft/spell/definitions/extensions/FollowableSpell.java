package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.spell.attributes.BooleanSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

public interface FollowableSpell extends ISpellDefinition {
    SpellAttribute<Boolean> FOLLOWS_PLAYER = new BooleanSpellAttribute("follow_player", false)
            .setWritable(true);

    default void setUpFollowing() {
        addAttribute(FOLLOWS_PLAYER);
    }
}
