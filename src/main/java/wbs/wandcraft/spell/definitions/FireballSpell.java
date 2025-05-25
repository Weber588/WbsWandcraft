package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Fireball;
import org.jetbrains.annotations.NotNull;
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

    @Override
    public Component description() {
        return Component.text("Shoots a fireball!");
    }

    @Override
    public @NotNull String getTexture() {
        return "spell_" + key().value();
    }
}
