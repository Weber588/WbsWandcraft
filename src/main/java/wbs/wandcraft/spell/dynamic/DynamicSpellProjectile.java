package wbs.wandcraft.spell.dynamic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.WbsParticleEffect;
import wbs.utils.util.particles.WbsParticleGroup;
import wbs.utils.util.particles.entity.DisplayParticleBuilder;
import wbs.utils.util.particles.entity.EntityParticle;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.cost.CostType;
import wbs.wandcraft.objects.generics.DynamicProjectileObject;
import wbs.wandcraft.spell.SpellType;
import wbs.wandcraft.spell.definitions.extensions.CustomProjectileSpell;
import wbs.wandcraft.spell.effect.SpellEffectDefinitions;
import wbs.wandcraft.spell.effect.SpellEffectInstance;
import wbs.wandcraft.spell.trigger.SpellTriggeredEvents;

import java.util.List;
import java.util.Set;

@NullMarked
public class DynamicSpellProjectile extends DynamicSpell implements CustomProjectileSpell {
    public static SpellAspect PROJECTILE_ASPECT = new GenericSpellAspect(
            "projectile",
            Component.text("Fires a projectile in the direction the caster is facing."),
            DynamicSpellProjectile::new
    );

    protected static final Set<Material> NATURE_BLOCKS = Set.of(
            Material.MOSS_BLOCK,
            Material.STONE,
            Material.COBBLESTONE,
            Material.MOSSY_COBBLESTONE
    );

    public DynamicSpellProjectile(SpellType primary, @Nullable SpellType secondary) {
        this(PROJECTILE_ASPECT, primary, secondary);
    }
    public DynamicSpellProjectile(SpellAspect aspect, SpellType primary, @Nullable SpellType secondary) {
        super(aspect, primary, secondary);

        setAttribute(GRAVITY, 0d);
    }

    @Override
    public void configure(DynamicProjectileObject projectile, CastContext context) {
        if (getPrimarySpellType() == SpellType.NATURE) {
            float size = (float) (double) context.instance().getAttribute(SIZE) * 1.7f;
            float speed  = (float) (double) context.instance().getAttribute(SPEED);

            Vector3fc axis = new Vector3f(1, 0, (float) Math.random() * 2 - 1);

            EntityParticle<BlockDisplay> particle = new DisplayParticleBuilder<>(BlockDisplay.class)
                    .rotationPivot(new Vector(-size / 2, -size / 2, -size / 2))
                    .setAngularVelocity(Vector.fromJOML(axis).normalize().multiply(speed / 2))
                    .setInterpolationDuration((int) Math.ceil(Ticks.TICKS_PER_SECOND / Bukkit.getServerTickManager().getTickRate()))
                    .editTransformation(t -> {
                        t.translate(-size / 2, -size / 2, -size / 2)
                                .scale(size);
                    })
                    .configure(display -> {
                        display.setBlock(WbsCollectionUtil.getRandom(NATURE_BLOCKS).createBlockData());
                    }).playParticle(context.location());

            projectile.follower(particle.getEntity());
            projectile.setTickEffects(null);
        } else {
            WbsParticleGroup particleGroup = getParticleGroup(
                    buildParticleEffect(projectile).setAmount(2),
                    buildParticleEffect(projectile).setAmount(1)
            );

            projectile.setTickEffects(particleGroup);
        }
        projectile.playEffectsOnTick(false);
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
