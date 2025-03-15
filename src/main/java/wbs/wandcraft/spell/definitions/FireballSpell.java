package wbs.wandcraft.spell.definitions;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;
import wbs.utils.util.entities.selector.RadiusSelector;
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
        context.instance().registerEffect(ON_HIT_TRIGGER.getAnonymousInstance((instance, effect, result) -> {
            explode(result, projectile.world, context);
        }));
    }

    @Override
    public Particle getDefaultParticle() {
        return Particle.FLAME;
    }

    private void explode(RayTraceResult result, World world, CastContext context) {
        Location location = result.getHitPosition().toLocation(world);
        SpellInstance instance = context.instance();

        double blastRadius = instance.getAttribute(BLAST_RADIUS);
        double damage = instance.getAttribute(DAMAGE);
        int duration = instance.getAttribute(DURATION);

        Entity hitEntity = result.getHitEntity();
        if (hitEntity != null) {
            context.player().damage(damage, hitEntity);
        }

        List<LivingEntity> selected = new RadiusSelector<>(LivingEntity.class)
                .setRange(blastRadius)
                .select(location);

        location.getWorld().createExplosion(context.player(), location, (float) blastRadius, false, false, true);

        for (LivingEntity hit : selected) {
            double distanceSquared = hit.getLocation().distanceSquared(location);
            int durationApplied;
            if (distanceSquared > 1) {
                durationApplied = (int) (duration / distanceSquared);
            } else {
                durationApplied = duration;
            }

            if (durationApplied > 0) {
                hit.setFireTicks(Math.max(hit.getFireTicks(), durationApplied));
            }
        }
    }
}
