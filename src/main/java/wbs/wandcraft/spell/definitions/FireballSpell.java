package wbs.wandcraft.spell.definitions;

import org.bukkit.entity.Fireball;
import wbs.wandcraft.spell.definitions.extensions.CastContext;
import wbs.wandcraft.spell.definitions.extensions.EntityProjectileSpell;

public class FireballSpell extends SpellDefinition implements EntityProjectileSpell<Fireball> {
    public FireballSpell() {
        super("fireball");
    }

    @Override
    public Class<Fireball> getProjectileClass() {
        return Fireball.class;
    }

    @Override
    public void configure(Fireball fireball, CastContext context) {

    }
}
