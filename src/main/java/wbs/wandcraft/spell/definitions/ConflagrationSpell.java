package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.utils.util.particles.DiscParticleEffect;
import wbs.utils.util.pluginhooks.WbsRegionUtils;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.extensions.BurnDamageSpell;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.ForceSpell;
import wbs.wandcraft.spell.definitions.extensions.RadiusedSpell;

import java.util.Collection;

import static wbs.wandcraft.spell.definitions.type.SpellType.NETHER;

public class ConflagrationSpell extends SpellDefinition implements CastableSpell, BurnDamageSpell, ForceSpell, RadiusedSpell {
    private final DiscParticleEffect popEffect = (DiscParticleEffect) new DiscParticleEffect()
            .setSpeed(1)
            .setAmount(10);
    private final DiscParticleEffect fireEffect = (DiscParticleEffect) new DiscParticleEffect()
            .setSpeed(0.05)
            .setAmount(15);

    public ConflagrationSpell() {
        super("conflagration");

        addSpellType(NETHER);

        setAttribute(COST, 100);
        setAttribute(COOLDOWN, 7 * Ticks.TICKS_PER_SECOND);

        setAttribute(DAMAGE, 2d);
        setAttribute(FORCE, 3d);
        setAttribute(BURN_TIME, 60);
        setAttribute(RADIUS, 4d);
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
        fireEffect.setRadius(radius).play(Particle.FLAME, context.location().add(0, -caster.getEyeHeight() + 0.1, 0));
        popEffect.setRadius(radius).play(Particle.LAVA, context.location().add(0, -caster.getEyeHeight(), 0));

        Collection<LivingEntity> hit = new RadiusSelector<>(LivingEntity.class)
                .setRange(radius)
                .selectExcluding(caster);

        for (LivingEntity target : hit) {
            if (WbsRegionUtils.canDealDamage(caster.getPlayer(), target)) {
                damageAndBurn(target, context);

                target.setVelocity(
                        target.getEyeLocation() // Give a slight upwards force by using eye height
                                .subtract(context.location())
                                .toVector()
                                .normalize()
                                .multiply(instance.getAttribute(FORCE))
                );
            }
        }
    }

    @Override
    public @NotNull String getKilledVerb() {
        return "burnt to a crisp";
    }
}
