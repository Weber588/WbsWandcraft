package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.particles.SphereParticleEffect;
import wbs.wandcraft.events.objects.MagicObjectMoveEvent;
import wbs.wandcraft.objects.MagicObjectManager;
import wbs.wandcraft.objects.colliders.Collision;
import wbs.wandcraft.objects.colliders.SphereCollider;
import wbs.wandcraft.objects.generics.DynamicMagicObject;
import wbs.wandcraft.objects.generics.DynamicProjectileObject;
import wbs.wandcraft.objects.generics.KinematicMagicObject;
import wbs.wandcraft.objects.generics.MagicObject;
import wbs.wandcraft.spell.attributes.BooleanSpellAttribute;
import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.extensions.*;

import java.util.List;
import java.util.Objects;

public class AntiMagicShellSpell extends SpellDefinition implements CastableSpell, RadiusedSpell, DurationalSpell, ParticleSpell {
    private static final SpellAttribute<Boolean> FOLLOWS_PLAYER = new BooleanSpellAttribute("follow_player", false);
    private static final SpellAttribute<Boolean> IS_REFLECTIVE = new BooleanSpellAttribute("is_reflective", true);
    private static final SpellAttribute<Integer> MAXIMUM_HITS = new IntegerSpellAttribute("maximum_hits", 6)
            .setShowAttribute(value -> value > 0);

    public AntiMagicShellSpell() {
        super("anti_magic_shell");

        addAttribute(FOLLOWS_PLAYER);
        addAttribute(IS_REFLECTIVE);
        addAttribute(MAXIMUM_HITS);
        setAttribute(DURATION, 600);
    }

    @Override
    public void cast(CastContext context) {
        Player player = context.player();

        AntiMagicShellObject shellObject = new AntiMagicShellObject(WbsEntityUtil.getMiddleLocation(player), player, context);

        shellObject.spawn();
    }

    @Override
    public Particle getDefaultParticle() {
        return Particle.HAPPY_VILLAGER;
    }

    public static class AntiMagicShellObject extends KinematicMagicObject implements Listener {
        public AntiMagicShellObject(Location location, Player caster, CastContext context) {
            super(location, caster, context);

            SpellInstance instance = castContext.instance();

            Double radius = instance.getAttribute(RADIUS);

            collider = new AntiMagicShellCollider(this, radius);
            collider.setBouncy(instance.getAttribute(IS_REFLECTIVE));
            setMaxAge(instance.getAttribute(DURATION));
            hits = instance.getAttribute(MAXIMUM_HITS);

            effect.setRadius(radius);
            effect.setAmount((int) (4 * Math.PI * radius * radius));

            ((AntiMagicShellCollider) collider).setRadius(radius);

            if (instance.getAttribute(FOLLOWS_PLAYER)) {
                collider.setPredicate(obj -> obj.getCaster() != caster);
            } else {
                collider.setPredicate(obj -> true);
            }
        }

        private int hits;

        private final SphereParticleEffect effect = new SphereParticleEffect();

        @Override
        protected void onRun() {
            effect.build();

            // Fizzle objects already in the shell
            List<MagicObject> insideShell = MagicObjectManager.getNearbyActive(getLocation(), castContext.instance().getAttribute(RADIUS));
            for (MagicObject object : insideShell) {
                if (object != this) object.remove(false);
            }
        }

        @Override
        protected boolean tick() {
            SpellInstance instance = castContext.instance();

            if (getAge() % 25 == 0) {
                effect.play(instance.getAttribute(PARTICLE, ((AntiMagicShellSpell) castContext.instance().getDefinition()).getDefaultParticle()), getLocation());
            }

            if (hits == 0) {
                return true;
            }

            if (instance.getAttribute(FOLLOWS_PLAYER)) {
                move(caster.getLocation());
            }

            return false;
        }

        private class AntiMagicShellCollider extends SphereCollider {
            public AntiMagicShellCollider(AntiMagicShellObject parent, double radius) {
                super(parent, radius);

                setCollideOnLeave(true);
                setCancelOnCollision(true);
            }

            @Override
            protected void beforeBounce(MagicObjectMoveEvent moveEvent, DynamicMagicObject dynamicObject) {
                Collision collision = Objects.requireNonNull(moveEvent.getCollision());

                collision.setNormal(collision.getNormal().add(WbsMath.randomVector(0.05)));
            }

            @Override
            protected void onBounce(MagicObjectMoveEvent moveEvent, DynamicMagicObject dynamicObject) {
                if (dynamicObject instanceof DynamicProjectileObject) {
                    // Allow reflected projectiles to hit the sender
                    dynamicObject.setEntityPredicate(LivingEntity.class::isInstance);
                }

                moveEvent.setCancelled(true);
            }

            @Override
            protected void onCollide(MagicObjectMoveEvent moveEvent, Collision collision) {
                hits--;
            }
        }
    }

    @Override
    public Component description() {
        return Component.text(
                "Form a shield around you that prevents any magic objects from entering or leaving"
        );
    }
}
