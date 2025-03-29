package wbs.wandcraft.spell.definitions;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.wandcraft.spell.definitions.extensions.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.DamageSpell;
import wbs.wandcraft.spell.definitions.extensions.RangedSpell;
import wbs.wandcraft.spell.event.SpellTriggeredEvents;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PrismaticRaySpell extends SpellDefinition implements CastableSpell, RangedSpell, DamageSpell {
    private static final double STEP_SIZE = 0.3;
    // TODO: Make beam width attribute
    private static final RadiusSelector<LivingEntity> radiusTargeter = new RadiusSelector<>(LivingEntity.class).setRange(0.2);

    public PrismaticRaySpell() {
        super("prismatic_ray");
    }

    @Override
    public void cast(CastContext context) {
        Player player = context.player();
        SpellInstance instance = context.instance();

        Location eyeLoc = player.getEyeLocation();

        double range = instance.getAttribute(RANGE);
        Location endLoc = WbsEntityUtil.getTargetPos(player, range);
        if (endLoc == null) {
            endLoc = eyeLoc.clone().add(WbsEntityUtil.getFacingVector(player, range));
        }

        World world = Objects.requireNonNull(eyeLoc.getWorld());

        double distance = endLoc.distance(eyeLoc);

        Vector direction = WbsEntityUtil.getFacingVector(player, STEP_SIZE);
        Location currentPos = eyeLoc.clone();

        Set<LivingEntity> alreadyHit = new HashSet<>();

        Particle display = Particle.INSTANT_EFFECT;
        double spread = 0.2;
        double damage = instance.getAttribute(DAMAGE);

        for (int i = 0; i <= distance/ STEP_SIZE; i++) {
            currentPos.add(direction);
            List<LivingEntity> hit = radiusTargeter.selectExcluding(currentPos, player);
            hit.removeAll(alreadyHit);
            hit.remove(player.getPlayer());
            for (LivingEntity target : hit) {
                if (damage > 0) {
                    player.damage(damage, target);
                }
                RayTraceResult result = new RayTraceResult(WbsEntityUtil.getMiddleLocation(target).toVector(), target);
                context.runEffects(SpellTriggeredEvents.ON_HIT_TRIGGER, result);
            }
            alreadyHit.addAll(hit);
            world.spawnParticle(display, currentPos, 2, spread, spread, spread, 0, null, true);
        }
    }
}
