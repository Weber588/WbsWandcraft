package wbs.wandcraft.spell.attributes.attributable;

import io.papermc.paper.registry.keys.tags.EntityTypeTagKeys;
import org.bukkit.Particle;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.WbsRegistryUtil;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.SpellType;
import wbs.wandcraft.spell.attributes.AttributeHolder;
import wbs.wandcraft.spell.attributes.DoubleSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;

public interface HealthAttributable extends AttributeHolder {
    NormalParticleEffect PARTICLE_EFFECT = (NormalParticleEffect) new NormalParticleEffect()
            .setAmount(3);

    SpellAttribute<Double> HEALTH = new DoubleSpellAttribute("health", 2)
            .addSuggestions(2.0, 5.0, 10.0, 20.0)
            .overrideTextureValue("health")
            .typeModifier(SpellType.NATURE, AttributeModifierType.MULTIPLY, 1.5)
            .typeModifier(SpellType.VOID, AttributeModifierType.MULTIPLY, 0.9);

    @AttributableSetupHandler
    default void setupHealth() {
        addAttribute(HEALTH);
    }

    default void heal(CastContext context, LivingEntity entity) {
        heal(context, entity, context.instance().getAttribute(HEALTH), null, null);
    }

    default void healWithParticles(CastContext context, LivingEntity entity) {
        healWithParticles(context, entity, context.instance().getAttribute(HEALTH));
    }
    default void healWithParticles(CastContext context, LivingEntity entity, double health) {
        heal(context, entity, health, () -> {
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

    default void heal(CastContext context, LivingEntity entity, @Nullable Runnable onHeal, @Nullable Runnable onDamage) {
        heal(context, entity, context.instance().getAttribute(HEALTH), onHeal, onDamage);
    }
    default void heal(CastContext context, LivingEntity entity, double health, @Nullable Runnable onHeal, @Nullable Runnable onDamage) {
        if (WbsRegistryUtil.isTagged(entity.getType(), EntityTypeTagKeys.UNDEAD)) {
            entity.damage(health, DamageSource.builder(DamageType.MAGIC).withDirectEntity(context.player()).build());
            if (onDamage != null) {
                onDamage.run();
            }
        } else {
            entity.heal(health, EntityRegainHealthEvent.RegainReason.MAGIC);
            if (onHeal != null) {
                onHeal.run();
            }
        }
    }
}
