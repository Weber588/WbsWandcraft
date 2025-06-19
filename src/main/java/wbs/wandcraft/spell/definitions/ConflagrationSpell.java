package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.utils.util.particles.DiscParticleEffect;
import wbs.utils.util.pluginhooks.WbsRegionUtils;
import wbs.wandcraft.spell.definitions.extensions.*;

import java.util.Collection;

public class ConflagrationSpell extends SpellDefinition implements CastableSpell, DamageSpell, SpeedSpell, BurnTimeSpell, RadiusedSpell {
    private final DiscParticleEffect popEffect = new DiscParticleEffect();
    private final DiscParticleEffect fireEffect = new DiscParticleEffect();

    public ConflagrationSpell() {
        super("conflagration");

        setAttribute(DAMAGE, 2d);
        setAttribute(SPEED, 0.25);
        setAttribute(BURN_TIME, 60);
        setAttribute(RADIUS, 4d);
    }

    @Override
    public Component description() {
        return Component.text("Throw out a wave of fire in all directions, repelling and burning nearby mobs.");
    }

    @Override
    public void cast(CastContext context) {
        Player caster = context.player();
        SpellInstance instance = context.instance();

        fireEffect.play(Particle.FLAME, caster.getLocation().add(0, 0.1, 0));
        popEffect.play(Particle.LAVA, caster.getLocation());

        Collection<LivingEntity> hit = new RadiusSelector<>(LivingEntity.class)
                .setRange(instance.getAttribute(RADIUS))
                .selectExcluding(caster);

        for (LivingEntity target : hit) {
            if (WbsRegionUtils.canDealDamage(caster.getPlayer(), target)) {
                WbsEntityUtil.damage(target, instance.getAttribute(DAMAGE), caster.getPlayer());
                target.setFireTicks((int) (instance.getAttribute(BURN_TIME) * (1 + (Math.random() * 0.4 - 0.2))));
                target.setVelocity(
                        target.getEyeLocation() // Give a slight upwards force by using eye height
                                .subtract(caster.getLocation())
                                .toVector()
                                .normalize()
                                .multiply(instance.getAttribute(SPEED))
                );
            }
        }
    }
}
