package wbs.wandcraft.spell.definitions;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.RayTraceResult;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.wandcraft.objects.generics.DynamicProjectileObject;
import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.extensions.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CustomProjectileSpell;
import wbs.wandcraft.spell.definitions.extensions.DamageSpell;
import wbs.wandcraft.spell.definitions.extensions.DurationalSpell;
import wbs.wandcraft.spell.event.SpellTriggeredEvents;

import java.util.List;

public class FireboltSpell extends SpellDefinition implements CustomProjectileSpell, DamageSpell, DurationalSpell {
    public static SpellAttribute<Double> BLAST_RADIUS = new DoubleSpellAttribute("blast_radius", 0,5.0)
            .setFormatter(radius -> radius + " blocks");

    public FireboltSpell() {
        super("firebolt");
        addAttribute(BLAST_RADIUS);
    }

    @Override
    public void configure(DynamicProjectileObject projectile, CastContext context) {
        SpellTriggeredEvents.ON_HIT_TRIGGER.registerAnonymous(context.instance(), (result) ->
                explode(result, projectile.world, context)
        );
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
        if (hitEntity != null && damage > 0) {
            context.player().damage(damage, hitEntity);
        }

        List<LivingEntity> selected = new RadiusSelector<>(LivingEntity.class)
                .setRange(blastRadius)
                .select(location);

        new NormalParticleEffect().play(Particle.EXPLOSION_EMITTER, location);

        for (LivingEntity hit : selected) {
            double distanceSquared = hit.getLocation().distanceSquared(location);
            int durationApplied;
            double damageApplied;
            if (distanceSquared <= 1) {
                durationApplied = duration;
                damageApplied = damage;
            } else {
                durationApplied = (int) (duration / distanceSquared);
                damageApplied = (int) (damage / distanceSquared);
            }

            if (durationApplied > 0) {
                hit.setFireTicks(Math.max(hit.getFireTicks(), durationApplied));
            }
            if (damageApplied > 0) {
                context.player().damage(damageApplied, hit);
            }
        }
    }
}
