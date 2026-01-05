package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.DamageSpell;
import wbs.wandcraft.spell.definitions.extensions.DirectionalSpell;
import wbs.wandcraft.spell.definitions.extensions.RangedSpell;
import wbs.wandcraft.spell.event.SpellTriggeredEvents;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static wbs.wandcraft.spell.definitions.type.SpellType.ARCANE;

public class PrismaticRaySpell extends SpellDefinition implements CastableSpell, RangedSpell, DamageSpell, DirectionalSpell {
    private static final double STEP_SIZE = 0.3;
    // TODO: Make beam width attribute
    private static final RadiusSelector<LivingEntity> radiusTargeter = new RadiusSelector<>(LivingEntity.class).setRange(1);

    public PrismaticRaySpell() {
        super("prismatic_ray");

        addSpellType(ARCANE);

        setAttribute(COST, 100);
        setAttribute(COOLDOWN, 10 * Ticks.TICKS_PER_SECOND);

        setAttribute(RANGE, 300d);
        setAttribute(IMPRECISION, 0d);
        setAttribute(DAMAGE, 10d);
    }

    @Override
    public void cast(CastContext context) {
        Player player = context.player();
        SpellInstance instance = context.instance();

        double range = instance.getAttribute(RANGE);
        Location endLoc = WbsEntityUtil.getTargetPos(player, range);
        if (endLoc == null) {
            endLoc = context.location().add(getDirection(context, range));
        }

        World world = context.world();

        double distance = endLoc.distance(context.location());

        Vector direction = getDirection(context, STEP_SIZE);
        Location currentPos = context.location();

        Set<LivingEntity> alreadyHit = new HashSet<>();

        Particle display = Particle.INSTANT_EFFECT;
        double spread = 0.2;
        double damage = instance.getAttribute(DAMAGE);

        DamageSource source = DamageSource.builder(DamageType.INDIRECT_MAGIC)
                .withDirectEntity(context.player())
                .build();

        for (int i = 0; i <= distance / STEP_SIZE; i++) {
            currentPos.add(direction);
            List<LivingEntity> hit = radiusTargeter.selectExcluding(currentPos, player);
            hit.removeAll(alreadyHit);
            hit.remove(player.getPlayer());
            for (LivingEntity target : hit) {
                if (damage > 0) {
                    target.damage(damage, source);
                }
                RayTraceResult result = new RayTraceResult(WbsEntityUtil.getMiddleLocation(target).toVector(), target);
                context.runEffects(SpellTriggeredEvents.ON_HIT_TRIGGER, result);
            }
            alreadyHit.addAll(hit);
            world.spawnParticle(display, currentPos, 2, spread, spread, spread, 0, null, true);
        }
    }

    @Override
    public String rawDescription() {
        return "A beam of energy is instantly sent out in the direct you're facing dealing damage to ALL creatures in its path.";
    }
}
