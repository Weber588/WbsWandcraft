package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.Fireball;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.extensions.EntityProjectileSpell;
import wbs.wandcraft.spell.definitions.type.SpellType;

public class FireballSpell extends SpellDefinition implements EntityProjectileSpell<Fireball> {
    public FireballSpell() {
        super("fireball");

        addSpellType(SpellType.NETHER);

        setAttribute(COST, 650);
        setAttribute(COOLDOWN, 20 * Ticks.TICKS_PER_SECOND);
    }

    @Override
    public Class<Fireball> getProjectileClass() {
        return Fireball.class;
    }

    @Override
    public void configure(Fireball fireball, CastContext context) {

    }

    @Override
    public String rawDescription() {
        return "Shoots a fireball!";
    }

    @Override
    public Component description() {
        return Component.text(rawDescription())
                .appendNewline()
                .append(Component.text("Warning! Fireballs can break blocks!").color(NamedTextColor.RED));
    }
}
