package wbs.wandcraft.spell.definitions;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;
import wbs.wandcraft.objects.generics.DynamicProjectileObject;
import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.extensions.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CustomProjectileSpell;
import wbs.wandcraft.spell.definitions.extensions.DamageSpell;
import wbs.wandcraft.spell.definitions.extensions.DurationalSpell;

import java.util.List;

public class FireballSpell extends SpellDefinition implements CustomProjectileSpell, DamageSpell, DurationalSpell {
    public static SpellAttribute<Double> BLAST_RADIUS = new DoubleSpellAttribute("blast_radius", 0,5.0);

    public FireballSpell() {
        super("fireball");
        addAttribute(BLAST_RADIUS);
    }

    @Override
    public void configure(DynamicProjectileObject projectile, CastContext context) {
        projectile.setOnHit((result) -> explode(result.getHitPosition().toLocation(projectile.world), context));
        projectile.setParticle(new WbsParticleGroup().addEffect(new NormalParticleEffect(), Particle.FLAME));
    }

    private boolean explode(Location location, CastContext context) {
        SpellInstance instance = context.instance();

        double blastRadius = instance.getAttribute(BLAST_RADIUS);
        double damage = instance.getAttribute(DAMAGE);
        int duration = instance.getAttribute(DURATION);

        List<LivingEntity> selected = new RadiusSelector<>(LivingEntity.class)
                .setRange(blastRadius)
                .select(location);

        for (LivingEntity hit : selected) {
            double distanceSquared = hit.getLocation().distanceSquared(location);
            double damageTaken;
            int durationApplied;
            if (distanceSquared > 1) {
                damageTaken = damage / distanceSquared;
                durationApplied = (int) (duration / distanceSquared);
            } else {
                damageTaken = damage;
                durationApplied = duration;
            }

            if (durationApplied > 0) {
                hit.setFireTicks(Math.max(hit.getFireTicks(), durationApplied));
            }

            if (damageTaken > 0) {
                hit.damage(damageTaken, context.player());
            }
        }

        return true;
    }
}
