package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.utils.util.particles.ElectricParticleEffect;
import wbs.utils.util.particles.SpiralParticleEffect;
import wbs.utils.util.particles.WbsParticleEffect;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.extensions.*;

import java.util.Collection;

import static wbs.wandcraft.spell.definitions.type.SpellType.ENDER;
import static wbs.wandcraft.spell.definitions.type.SpellType.VOID;

public class VoidStepSpell extends SpellDefinition implements CastableSpell, RangedSpell, SpeedSpell, DirectionalSpell, DamageSpell, RadiusedSpell {
    private static final ElectricParticleEffect EFFECT = (ElectricParticleEffect) new ElectricParticleEffect()
            .setTicks(40)
            .setRadius(0.6)
            .setAmount(3)
            .setData(new Particle.DustOptions(Color.fromRGB(160, 120, 255), 0.4F));
    private static final WbsParticleEffect SMOKE_EFFECT = new SpiralParticleEffect()
            .setRadius(0.5)
            .setSpeed(0.3)
            .setRelative(true)
            .setAmount(8);

    public VoidStepSpell() {
        super("void_step");

        addSpellType(ENDER);
        addSpellType(VOID);

        setAttribute(COST, 500);
        setAttribute(COOLDOWN, 2.5 * Ticks.TICKS_PER_SECOND);

        setAttribute(RANGE, 10.0);
        setAttribute(RADIUS, 10.0);
        setAttribute(DAMAGE, 4d);
        setAttribute(IMPRECISION, 5d);
        setAttribute(SPEED, 1.5d);
    }

    @Override
    public void cast(CastContext context) {
        Player player = context.player();
        Location loc = player.getLocation();

        SMOKE_EFFECT.play(Particle.SMOKE, loc);
        EFFECT.play(Particle.DUST, WbsEntityUtil.getMiddleLocation(player));

        double range = context.instance().getAttribute(RANGE);
        Vector direction = getDirection(context, range);
        Block tpLocation = WbsEntityUtil.getSafeLocation(player, player.getLocation().add(direction), range);

        RadiusSelector<LivingEntity> selector = new RadiusSelector<>(LivingEntity.class)
                .setRange(context.instance().getAttribute(RADIUS));

        Collection<LivingEntity> hits = selector.selectExcluding(player);

        if (tpLocation != null) {
            player.teleport(tpLocation.getLocation().setDirection(WbsEntityUtil.getFacingVector(player)));
            loc = player.getLocation();

            SMOKE_EFFECT.play(Particle.SMOKE, loc);
            EFFECT.play(Particle.DUST, WbsEntityUtil.getMiddleLocation(player));

            for (LivingEntity hit : hits) {
                damageThen(hit, context, damageable -> {
                    PotionEffect effect = new PotionEffect(PotionEffectType.BLINDNESS, (int) (Math.random() * 200), 0);
                    hit.addPotionEffect(effect);

                    effect = new PotionEffect(PotionEffectType.SLOWNESS, (int) (Math.random() * 200), 0);
                    hit.addPotionEffect(effect);
                });
            }

            WbsEntityUtil.push(player, context.instance().getAttribute(SPEED));
        } else {
            WbsWandcraft.getInstance().sendActionBar("No safe space found!", player);
        }
    }

    @Override
    public String rawDescription() {
        return "The caster is teleported a short distance in the direction they're facing.";
    }
}
