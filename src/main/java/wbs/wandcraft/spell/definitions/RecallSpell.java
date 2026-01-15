package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import wbs.utils.util.WbsSound;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.particles.RingParticleEffect;
import wbs.utils.util.string.WbsStringify;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.objects.MagicObjectManager;
import wbs.wandcraft.objects.generics.MagicObject;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.DurationalSpell;

import java.time.Duration;
import java.util.Collection;

import static wbs.wandcraft.spell.definitions.type.SpellType.ARCANE;
import static wbs.wandcraft.spell.definitions.type.SpellType.ENDER;

public class RecallSpell extends SpellDefinition implements CastableSpell, DurationalSpell {
    private static final NormalParticleEffect TELEPORT_EFFECT = (NormalParticleEffect) new NormalParticleEffect()
            .setXYZ(0)
            .setSpeed(0.02)
            .setAmount(120);

    public RecallSpell() {
        super("recall");

        setAttribute(COST, 100);
        setAttribute(COOLDOWN, 5 * Ticks.TICKS_PER_SECOND);

        addSpellType(ENDER);
        addSpellType(ARCANE);

        setAttribute(DURATION, 0);
    }

    @Override
    public void cast(CastContext context) {
        Player player = context.player();

        Collection<MagicObject> ownedObjects = MagicObjectManager.getAllActive(player);

        RecallPoint point = null;
        for (MagicObject obj : ownedObjects) {
            if (obj instanceof RecallPoint) {
                point = (RecallPoint) obj;
                break;
            }
        }

        if (point == null) {
            createPoint(context);
        } else {
            recall(player, point);
        }
    }


    private final WbsSound sound = new WbsSound(Sound.ENTITY_ENDERMAN_TELEPORT);

    public void recall(Player player, RecallPoint point) {
        TELEPORT_EFFECT.play(Particle.DRAGON_BREATH,
                WbsEntityUtil.getMiddleLocation(player));

        player.setFallDistance(0);
        player.teleport(point.getLocation());

        TELEPORT_EFFECT.play(Particle.DRAGON_BREATH, WbsEntityUtil.getMiddleLocation(player));

        sound.play(point.getLocation());
        WbsWandcraft.getInstance().sendActionBar("Recalled!", player);
        point.remove();
    }

    private void createPoint(CastContext context) {
        Player player = context.player();
        SpellInstance instance = context.instance();

        Location spawnLoc;
        //noinspection deprecation
        if (!player.isOnGround()) {
            World world = player.getLocation().getWorld();
            assert world != null;

            RayTraceResult result = world.rayTraceBlocks(player.getLocation(), new Vector(0, -1, 0), 255.0, FluidCollisionMode.ALWAYS, true);

            if (result == null) {
                spawnLoc = player.getLocation();
            } else {
                spawnLoc = result.getHitPosition().toLocation(world);
                spawnLoc.setPitch(player.getLocation().getPitch());
                spawnLoc.setYaw(player.getLocation().getYaw());
            }
        } else {
            spawnLoc = player.getLocation();
        }

        int duration = instance.getAttribute(DURATION);

        RecallPoint newPoint = new RecallPoint(spawnLoc, context, duration);
        newPoint.spawn();

        if (duration > 0) {
            WbsWandcraft.getInstance().sendActionBar("You will return to this point in &h" + WbsStringify.toString(Duration.ofSeconds(duration / 20), false) + "&r!", player);
        }
    }

    public class RecallPoint extends MagicObject {
        private int age = 0;
        private final int duration;

        private final NormalParticleEffect effect;
        private final RingParticleEffect ringEffect;

        public RecallPoint(Location location, CastContext castContext, int duration) {
            super(location, castContext);
            this.duration = duration;

            effect = new NormalParticleEffect().setXYZ(0.05);
            effect.setAmount(2);

            ringEffect = new RingParticleEffect();
            ringEffect.setRadius(0.5).setAmount(3);
            ringEffect.setData(Bukkit.createBlockData(Material.PURPLE_WOOL));
        }

        @Override
        protected void onRemove() {
            recall(caster, this);
        }

        @Override
        protected boolean tick() {
            age++;
            if (duration > 0 && age > duration) {
                recall(caster, this);
                return true;
            }
            effect.play(Particle.WITCH, getLocation());
            ringEffect.setRotation(age * 6);
            ringEffect.buildAndPlay(Particle.FALLING_DUST, getLocation().add(0, 1, 0));
            return false;
        }
    }

    @Override
    public String rawDescription() {
        return "Leave behind a magic checkpoint, that you return to when you cast it again!";
    }
}
