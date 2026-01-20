package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import wbs.utils.util.WbsSound;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.utils.util.particles.RingParticleEffect;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.extensions.*;

import java.util.Collection;

import static wbs.wandcraft.spell.definitions.type.SpellType.ARCANE;
import static wbs.wandcraft.spell.definitions.type.SpellType.VOID;

// TODO: Make this a continuous cast spell
public class ArcaneSurgeSpell extends SpellDefinition implements CastableSpell, DurationalSpell, DamageSpell, SpeedSpell, ParticleSpell {
    private final Particle.DustOptions data = new Particle.DustOptions(ARCANE.mulColor(1.2), 0.6F);
    private final Particle.DustOptions dataCore = new Particle.DustOptions(VOID.mulColor(0.5), 1F);

    private final PotionEffect effect = new PotionEffect(PotionEffectType.RESISTANCE, 20, 7);

    private final RingParticleEffect particleEffect = (RingParticleEffect) new RingParticleEffect()
            .setAmount(75)
            .setData(data);
    private final RingParticleEffect coreEffect = (RingParticleEffect) new RingParticleEffect()
            .setAmount(75)
            .setData(dataCore);

    public ArcaneSurgeSpell() {
        super("arcane_surge");

        addSpellType(ARCANE);
        addSpellType(VOID);

        setAttribute(COST, 200);
        setAttribute(COOLDOWN, 8 * Ticks.TICKS_PER_SECOND);
    }

    @Override
    public void cast(CastContext context) {
        SpellInstance instance = context.instance();
        Player player = context.player();

        int duration = instance.getAttribute(DURATION);
        double speed = instance.getAttribute(SPEED);

        Vector velocity = WbsEntityUtil.getFacingVector(player, speed);

        player.addPotionEffect(effect);

        WbsSound sound = new WbsSound(Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED);

        double hitbox = 2;

        RingParticleEffect particleEffect = (RingParticleEffect) this.particleEffect.clone()
                .setRadius(hitbox/2)
                .setAbout(velocity);
        RingParticleEffect coreEffect = (RingParticleEffect) this.coreEffect.clone()
                .setRadius(hitbox/4)
                .setAbout(velocity);

        new BukkitRunnable() {
            int i = 0;
            Collection<LivingEntity> entities;

            Location playerLoc = player.getEyeLocation();

            public void run() {
                playerLoc = player.getEyeLocation();
                i++;
                if (i > duration) {
                    // Handle finish manually
                    context.finish();
                    cancel();
                }
                entities = new RadiusSelector<>(LivingEntity.class)
                        .setRange(hitbox)
                        .exclude(player)
                        .select(WbsEntityUtil.getMiddleLocation(player));

                for (LivingEntity e : entities) {
                    damage(context, e);
                }

                sound.play(playerLoc);

                Particle particle = getParticle(context.instance());

                particleEffect.play(particle, playerLoc);
                coreEffect.play(particle, playerLoc);

                player.setVelocity(velocity);
            }
        }.runTaskTimer(WbsWandcraft.getInstance(), 0L, 1L);
    }

    @Override
    public boolean completeAfterCast() {
        return false;
    }

    @Override
    public Particle getDefaultParticle() {
        return Particle.DUST;
    }

    @Override
    public String rawDescription() {
        return "The caster moves forward for a set distance, dealing damage to nearby creatures. " +
                        "The caster is immune to all damage while moving.";
    }
}
