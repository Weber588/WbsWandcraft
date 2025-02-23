package wbs.wandcraft.spell.definitions.extensions;

import wbs.wandcraft.spell.definitions.AbstractSpellDefinition;

public interface CastableSpell extends AbstractSpellDefinition {
    void cast(CastContext context);
}
