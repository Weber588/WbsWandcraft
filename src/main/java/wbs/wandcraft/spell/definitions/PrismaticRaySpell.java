package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import wbs.utils.util.WbsColour;
import wbs.utils.util.WbsColours;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.DamageSpell;
import wbs.wandcraft.spell.definitions.extensions.RaySpell;

import java.util.Set;

import static wbs.wandcraft.spell.definitions.type.SpellType.ARCANE;

public class PrismaticRaySpell extends SpellDefinition implements CastableSpell, RaySpell, DamageSpell {

    public static final @NotNull Color START_COLOR = Color.fromRGB(0xFF7575);
    public static final @NotNull Color END_COLOR = Color.fromRGB(0xFF7578);

    public PrismaticRaySpell() {
        super("prismatic_ray");

        addSpellType(ARCANE);

        setAttribute(COST, 100);
        setAttribute(COOLDOWN, 10 * Ticks.TICKS_PER_SECOND);

        setAttribute(RANGE, 300d);
        setAttribute(RADIUS, 0.4d);
        setAttribute(IMPRECISION, 0d);
        setAttribute(DAMAGE, 10d);
    }

    @Override
    public String rawDescription() {
        return "A beam of energy is instantly sent out in the direction you're facing, dealing damage to ALL creatures in its path.";
    }

    @Override
    public void onHitEntity(CastContext context, Location currentPos, LivingEntity target) {
        double damage = context.instance().getAttribute(DAMAGE);

        if (damage > 0) {
            damage(context, target, damage);
        }
    }

    @Override
    public boolean onStep(CastContext context, Location currentPos, Set<LivingEntity> alreadyHit, int currentStep, int maxSteps) {
        double particleRadius = context.instance().getAttribute(RADIUS) / 2;

        Color color;
        double interval = (double) (currentStep) / maxSteps;
        color = WbsColours.colourLerp(START_COLOR, END_COLOR, interval);

        currentPos.getWorld().spawnParticle(
                Particle.INSTANT_EFFECT,
                currentPos,
                1,
                particleRadius,
                particleRadius,
                particleRadius,
                0,
                new Particle.Spell(color, 1f),
                true
        );

        return false;
    }

    @Override
    public boolean canHitEntities() {
        return true;
    }

    @Override
    public double getStepSize() {
        return 0.1;
    }
}
