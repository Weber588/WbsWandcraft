package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsMath;
import wbs.utils.util.particles.RingParticleEffect;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.extensions.RaySpell;
import wbs.wandcraft.spell.definitions.type.SpellType;

import java.util.Set;

public class GrowSpell extends SpellDefinition implements RaySpell {
    private static final RingParticleEffect PARTICLE_EFFECT = (RingParticleEffect) new RingParticleEffect()
            .setRadius(0.35F)
            .setAmount(2);

    public GrowSpell() {
        super("grow");

        addSpellType(SpellType.NATURE);

        setAttribute(COST, 75);
        setAttribute(COOLDOWN, (int) (0.5 * Ticks.TICKS_PER_SECOND));

        setAttribute(RANGE, 20d);
        setAttribute(RADIUS, 0.4d);
        setAttribute(IMPRECISION, 0d);
    }

    @Override
    public void onHitBlock(CastContext context, @NotNull Block hitBlock, @NotNull BlockFace hitBlockFace) {
        hitBlock.applyBoneMeal(hitBlockFace);
    }

    @Override
    public boolean onStep(CastContext context, Location currentPos, Set<LivingEntity> alreadyHit, int currentStep, int maxSteps) {
        PARTICLE_EFFECT.setRotation(currentStep * getStepSize() * 150)
                .setAbout(WbsMath.getFacingVector(context.player()))
                .buildAndPlay(Particle.HAPPY_VILLAGER, currentPos);

        return false;
    }

    @Override
    public boolean canHitEntities() {
        return false;
    }

    @Override
    public double getStepSize() {
        return 0.2;
    }

    @Override
    public String rawDescription() {
        return "Applies bone meal on the target block";
    }
}
