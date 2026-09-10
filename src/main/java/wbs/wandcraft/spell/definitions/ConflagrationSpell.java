package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.utils.util.particles.DiscParticleEffect;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.extensions.BurnDamageSpell;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.ForceSpell;
import wbs.wandcraft.spell.definitions.extensions.RadiusedSpell;

import java.util.Collection;

import static wbs.wandcraft.spell.definitions.type.SpellType.NETHER;

public class ConflagrationSpell extends SpellDefinition implements CastableSpell, BurnDamageSpell, ForceSpell, RadiusedSpell {
    public static final double DAMAGE_RANGE = 3d;
    private final DiscParticleEffect popEffect = (DiscParticleEffect) new DiscParticleEffect()
            .setSpeed(3)
            .setAmount(10);
    private final DiscParticleEffect fireEffect = (DiscParticleEffect) new DiscParticleEffect()
            .setRandom(true)
            .setRelative(true)
            .setSpeed(0.4)
            .setAmount(45);

    public ConflagrationSpell() {
        super("conflagration");

        addSpellType(NETHER);

        setAttribute(COST, 100);
        setAttribute(COOLDOWN, 7 * Ticks.TICKS_PER_SECOND);

        setAttribute(DAMAGE, 2d);
        setAttribute(FORCE, 2d);
        setAttribute(BURN_TIME, 60);
        setAttribute(RADIUS, 6d);
    }

    @Override
    public String rawDescription() {
        return "Throw out a wave of fire in all directions, repelling and burning nearby mobs.";
    }

    @Override
    public void cast(CastContext context) {
        Player caster = context.player();
        SpellInstance instance = context.instance();

        double radius = instance.getAttribute(RADIUS);
        fireEffect.setRadius(radius / 4)
                .setAmount((int) (radius * radius * Math.PI / 2))
                .play(Particle.FLAME, context.location().add(0, -caster.getEyeHeight() + 0.1, 0));
        popEffect.setRadius(radius)
                .play(Particle.LAVA, context.location().add(0, -caster.getEyeHeight(), 0));

        Collection<LivingEntity> hit = new RadiusSelector<>(LivingEntity.class)
                .setRange(radius)
                .exclude(caster)
                .select(context.location());

        for (LivingEntity target : hit) {
            double damage = scaleByDistance(context, target, DAMAGE_RANGE, DAMAGE);
            int burnTime = scaleByDistance(context, target, DAMAGE_RANGE, BURN_TIME);
            damageAndBurn(target, context, damage, burnTime);

            Vector centerToTarget = target.getEyeLocation() // Give a slight upwards force by using eye height
                    .subtract(context.location())
                    .toVector();

            target.setVelocity(centerToTarget.normalize()
                    .multiply(scaleByDistance(context, target, 2, FORCE))
            );
        }
    }

    @Override
    public @NotNull String getKilledVerb() {
        return "burnt to a crisp";
    }
}
