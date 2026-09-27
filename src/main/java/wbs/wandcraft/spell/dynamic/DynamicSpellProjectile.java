package wbs.wandcraft.spell.dynamic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.cost.CostType;
import wbs.wandcraft.objects.generics.DynamicProjectileObject;
import wbs.wandcraft.spell.SpellType;
import wbs.wandcraft.spell.definitions.extensions.CustomProjectileSpell;
import wbs.wandcraft.spell.effect.SpellEffectDefinitions;
import wbs.wandcraft.spell.effect.SpellEffectInstance;
import wbs.wandcraft.spell.trigger.SpellTriggeredEvents;

import java.util.List;

@NullMarked
public class DynamicSpellProjectile extends DynamicSpell implements CustomProjectileSpell {
    public static SpellAspect PROJECTILE_ASPECT = new GenericSpellAspect("projectile", DynamicSpellProjectile::new);

    public DynamicSpellProjectile(SpellType primary, @Nullable SpellType secondary) {
        super("projectile", primary, secondary);

        setAttribute(GRAVITY, 0d);
    }

    @Override
    public void configure(DynamicProjectileObject projectile, CastContext context) {
        WbsParticleGroup particleGroup = getParticleGroup(
                buildParticleEffect(projectile).setAmount(2),
                buildParticleEffect(projectile).setAmount(1)
        );

        projectile.setTickEffects(particleGroup);
    }

    private static WbsParticleEffect buildParticleEffect(DynamicProjectileObject projectile) {
        return new NormalParticleEffect().setXYZ(projectile.getHitBoxSize() / 3);
    }

    @Override
    protected Multimap<SpellType, SpellEffectInstance<?>> typedEvents() {
        HashMultimap<SpellType, SpellEffectInstance<?>> typedEvents = HashMultimap.create();

        typedEvents.put(
                SpellType.NETHER,
                // Damage is already handled by attributes
                SpellTriggeredEvents.ON_HIT_TRIGGER.getInstance(SpellEffectDefinitions.IGNITE)
        );

        typedEvents.put(
                SpellType.ENDER,
                SpellTriggeredEvents.ON_HIT_TRIGGER.getInstance(SpellEffectDefinitions.RANDOM_TELEPORT)
        );

        typedEvents.put(
                SpellType.SCULK,
                SpellTriggeredEvents.ON_HIT_TRIGGER.getAnonymousInstance(((context, effectInstance, result) -> {
                    Entity hitEntity = result.getHitEntity();
                    if (hitEntity instanceof LivingEntity entity) {
                        PotionEffect effect = WbsCollectionUtil.getRandom(CostType.FATIGUE_EFFECTS);
                        entity.addPotionEffect(effect);
                    }
                }))
        );

        typedEvents.putAll(
                SpellType.NATURE,
                List.of(
                        SpellTriggeredEvents.ON_HIT_TRIGGER.getInstance(SpellEffectDefinitions.HEAL),
                        SpellTriggeredEvents.ON_HIT_TRIGGER.getInstance(SpellEffectDefinitions.GROW)
                )
        );

        return typedEvents;
    }
}
