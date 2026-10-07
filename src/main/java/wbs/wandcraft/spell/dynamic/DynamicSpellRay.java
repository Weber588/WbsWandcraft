package wbs.wandcraft.spell.dynamic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.ParticleDataProvider;
import wbs.utils.util.particles.WbsParticleGroup;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.cost.CostType;
import wbs.wandcraft.spell.MagicDomain;
import wbs.wandcraft.spell.definitions.extensions.RaySpell;
import wbs.wandcraft.spell.effect.SpellEffectDefinitions;
import wbs.wandcraft.spell.effect.SpellEffectInstance;
import wbs.wandcraft.spell.trigger.SpellTriggeredEvents;

import java.util.List;
import java.util.Set;

@NullMarked
public class DynamicSpellRay extends DynamicSpell implements RaySpell {
    public static SpellArchetype RAY = new GenericSpellArchetype(
            "ray",
            Component.text("Project a ray in the direction the caster is facing, instantly affecting everything in the path."),
            DynamicSpellRay::new
    );
    private final WbsParticleGroup particleGroup;

    public DynamicSpellRay(MagicDomain primary, @Nullable MagicDomain secondary) {
        super(RAY, primary, secondary);

        particleGroup = getParticleGroup(
                new NormalParticleEffect().setAmount(2),
                new NormalParticleEffect().setAmount(1)
        ).setPlayFunction(((effect, location, particle) -> {
            effect.build();
            ParticleDataProvider.playEffectSafely(effect, location, particle, this);
        }));

        setAttribute(RADIUS, 0.4d);
        setAttribute(IMPRECISION, 0.5d);
    }

    @Override
    public boolean onStep(CastContext context, Location currentPos, Set<LivingEntity> alreadyHit, int currentStep, int maxSteps) {
        double radius = context.instance().getAttribute(RADIUS);
        particleGroup.effects().keySet().forEach(effect -> {
            if (effect instanceof NormalParticleEffect nEffect) {
                nEffect.setXYZ(radius / 5);
            }
        });

        particleGroup.play(currentPos);
        return false;
    }

    @Override
    public boolean canHitEntities() {
        return true;
    }

    @Override
    public double getStepSize() {
        return 0.3;
    }

    @Override
    protected Multimap<MagicDomain, SpellEffectInstance<?>> typedEvents() {
        HashMultimap<MagicDomain, SpellEffectInstance<?>> typedEvents = HashMultimap.create();

        typedEvents.put(
                MagicDomain.NETHER,
                // Damage is already handled by attributes
                SpellTriggeredEvents.ON_HIT_TRIGGER.getInstance(SpellEffectDefinitions.IGNITE)
        );

        typedEvents.put(
                MagicDomain.ENDER,
                SpellTriggeredEvents.ON_HIT_TRIGGER.getInstance(SpellEffectDefinitions.RANDOM_TELEPORT)
        );

        typedEvents.put(
                MagicDomain.SCULK,
                SpellTriggeredEvents.ON_HIT_TRIGGER.getAnonymousInstance(((context, effectInstance, result) -> {
                    Entity hitEntity = result.getHitEntity();
                    if (hitEntity instanceof LivingEntity entity) {
                        PotionEffect effect = WbsCollectionUtil.getRandom(CostType.FATIGUE_EFFECTS);
                        entity.addPotionEffect(effect);
                    }
                }))
        );

        typedEvents.putAll(
                MagicDomain.NATURE,
                List.of(
                        SpellTriggeredEvents.ON_HIT_TRIGGER.getInstance(SpellEffectDefinitions.HEAL),
                        SpellTriggeredEvents.ON_HIT_TRIGGER.getInstance(SpellEffectDefinitions.GROW)
                )
        );

        return typedEvents;
    }
}
