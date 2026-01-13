package wbs.wandcraft.spell.definitions.extensions;

import io.papermc.paper.registry.keys.tags.EntityTypeTagKeys;
import org.bukkit.Particle;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import wbs.utils.util.WbsRegistryUtil;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.ISpellDefinition;

public interface HealthSpell extends ISpellDefinition {
    NormalParticleEffect PARTICLE_EFFECT = (NormalParticleEffect) new NormalParticleEffect()
            .setAmount(3);

    SpellAttribute<Double> HEALTH = new DoubleSpellAttribute("health", 2)
            .addSuggestions(2.0, 5.0, 10.0, 20.0)
            .overrideTextureValue("health");

    default void setupHealth() {
        addAttribute(HEALTH);
    }

    default void heal(CastContext context, LivingEntity entity) {
        heal(context, entity, () -> {}, () -> {});
    }

    default void healWithParticles(CastContext context, LivingEntity entity) {
        heal(context, entity, () -> {
            PARTICLE_EFFECT
                    .setXYZ(entity.getWidth() / 2)
                    .setY(entity.getHeight() / 2)
                    .play(Particle.HEART, WbsEntityUtil.getMiddleLocation(entity));
        }, () -> {
            PARTICLE_EFFECT
                    .setXYZ(entity.getWidth() / 2)
                    .setY(entity.getHeight() / 2)
                    .play(Particle.DAMAGE_INDICATOR, WbsEntityUtil.getMiddleLocation(entity));
        });
    }

    default void heal(CastContext context, LivingEntity entity, Runnable onHeal, Runnable onDamage) {
        double health = context.instance().getAttribute(HEALTH);

        if (WbsRegistryUtil.isTagged(entity.getType(), EntityTypeTagKeys.UNDEAD)) {
            entity.damage(health, DamageSource.builder(DamageType.MAGIC).withDirectEntity(context.player()).build());
            onDamage.run();
        } else {
            entity.heal(health, EntityRegainHealthEvent.RegainReason.MAGIC);
            onHeal.run();
        }
    }
}
