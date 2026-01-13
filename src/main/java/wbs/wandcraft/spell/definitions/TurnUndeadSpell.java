package wbs.wandcraft.spell.definitions;

import com.destroystokyo.paper.entity.ai.VanillaGoal;
import io.papermc.paper.registry.keys.tags.EntityTypeTagKeys;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsRegistryUtil;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.wandcraft.ai.CustomAvoidGoal;
import wbs.wandcraft.ai.TemporaryGoal;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.DurationalSpell;
import wbs.wandcraft.spell.definitions.extensions.TargetedSpell;
import wbs.wandcraft.spell.definitions.type.SpellType;

public class TurnUndeadSpell extends SpellDefinition implements CastableSpell, DurationalSpell, TargetedSpell<Mob> {
    private static final NormalParticleEffect EFFECT = (NormalParticleEffect) new NormalParticleEffect()
            .setSpeed(0.05)
            .setAmount(15);

    public TurnUndeadSpell() {
        super("turn_undead");

        addSpellType(SpellType.NETHER);
        addSpellType(SpellType.SCULK);

        setAttribute(COST, 350);
        setAttribute(COOLDOWN, 60 * Ticks.TICKS_PER_SECOND);

        setAttribute(DURATION, 15 * Ticks.TICKS_PER_SECOND);
        setAttribute(TARGET, TargeterType.RADIUS);
        setAttribute(TARGET_RANGE, 20d);
        setAttribute(MAX_TARGETS, 10);
    }

    @Override
    public void cast(CastContext context) {
        int duration = context.instance().getAttribute(DURATION);
        double range = context.instance().getAttribute(TARGET_RANGE);

        getTargets(context).forEach(target -> {
            EFFECT.setXYZ(target.getWidth()).setY(target.getHeight()).play(Particle.RAID_OMEN, WbsEntityUtil.getMiddleLocation(target));
            target.getPathfinder().stopPathfinding();
            target.setTarget(null);

            TemporaryGoal<@NotNull Mob> goal = new CustomAvoidGoal(target, new RadiusSelector<>(LivingEntity.class).setRange(range), 1.3)
                    .maxAge(duration);

            TemporaryGoal.replaceGoal(target, goal, 2, VanillaGoal.NEAREST_ATTACKABLE);
        });
    }

    @Override
    public boolean isValid(Mob entity) {
        return TargetedSpell.super.isValid(entity) && WbsRegistryUtil.isTagged(entity.getType(), EntityTypeTagKeys.UNDEAD);
    }

    @Override
    public String rawDescription() {
        return "Makes all undead in a radius flee from you.";
    }

    @Override
    public Class<Mob> getEntityClass() {
        return Mob.class;
    }
}
