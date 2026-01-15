package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.LineParticleEffect;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.TargetedSpell;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static wbs.wandcraft.spell.definitions.type.SpellType.ENDER;
import static wbs.wandcraft.spell.definitions.type.SpellType.SCULK;

public class DisplaceSpell extends SpellDefinition implements CastableSpell, TargetedSpell<LivingEntity> {
    private static final LineParticleEffect LINE_EFFECT = (LineParticleEffect) new LineParticleEffect()
            .setScaleAmount(true)
            .setRadius(0.1)
            .setAmount(5);

    public DisplaceSpell() {
        super("displace");

        setAttribute(COST, 150);
        setAttribute(COOLDOWN, 5 * Ticks.TICKS_PER_SECOND);

        addSpellType(ENDER);
        addSpellType(SCULK);
    }

    @Override
    public String rawDescription() {
        return "Swaps the caster with a random target.";
    }

    @Override
    public void cast(CastContext context) {
        Player player = context.player();

        List<LivingEntity> toSwap = new LinkedList<>(getTargets(context));
        toSwap.add(player);

        Collections.shuffle(toSwap);

        List<Location> targetLocations = toSwap.stream()
                .map(Entity::getLocation)
                .collect(Collectors.toCollection(LinkedList::new));

        // Shift so no entry maps to itself
        Location first = targetLocations.removeFirst();
        targetLocations.add(first);

        World world = context.world();
        for (int i = 0; i < toSwap.size(); i++) {
            Location targetLocation = targetLocations.get(i);
            LivingEntity entity = toSwap.get(i);

            Location originalLocation = WbsEntityUtil.getMiddleLocation(entity);

            entity.teleport(targetLocation);

            targetLocation.add(0, entity.getHeight() / 2, 0);

            LINE_EFFECT.play(Particle.DRAGON_BREATH, originalLocation, targetLocation);

            world.spawnParticle(Particle.DRAGON_BREATH, targetLocation, 25, 0.15, 0.15, 0.15, 0);
            world.spawnParticle(Particle.WITCH, targetLocation, 400, 0.6, 1, 0.6, 0);
        }
    }

    @Override
    public Class<LivingEntity> getEntityClass() {
        return LivingEntity.class;
    }
}
