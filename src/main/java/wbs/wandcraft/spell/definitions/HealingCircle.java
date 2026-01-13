package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.utils.util.pluginhooks.PacketEventsWrapper;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.context.CastingManager;
import wbs.wandcraft.objects.generics.MagicObject;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.DurationalSpell;
import wbs.wandcraft.spell.definitions.extensions.HealthSpell;
import wbs.wandcraft.spell.definitions.extensions.RadiusedSpell;
import wbs.wandcraft.spell.definitions.type.SpellType;
import wbs.wandcraft.util.EffectUtils;

import java.util.LinkedList;
import java.util.List;

public class HealingCircle extends SpellDefinition implements CastableSpell, HealthSpell, RadiusedSpell, DurationalSpell {
    private static final int GLYPHS_PER_BLOCK = 3;

    public HealingCircle() {
        super("healing_circle");

        addSpellType(SpellType.NATURE);
        addSpellType(SpellType.ARCANE);

        setAttribute(COST, 350);
        setAttribute(COOLDOWN, 60 * Ticks.TICKS_PER_SECOND);

        setAttribute(HEALTH, 2d);
        setAttribute(RADIUS, 2d);
        setAttribute(DURATION, 15 * Ticks.TICKS_PER_SECOND);
    }

    @Override
    public void cast(CastContext context) {
        HealingCircleObject object = new HealingCircleObject(context.player().getLocation(), context.player(), context);

        CastingManager.setConcentrating(context.player(), context);
        object.spawn();
    }

    @Override
    public String rawDescription() {
        return "Create a circle of runes that heal anything in its radius.";
    }

    private class HealingCircleObject extends MagicObject {
        private final List<TextDisplay> displays = new LinkedList<>();
        private final double radius;
        private final RadiusSelector<LivingEntity> selector;

        public HealingCircleObject(Location location, Player caster, @NotNull CastContext context) {
            super(location.setRotation(0, 0), caster, context);

            radius = context.instance().getAttribute(RADIUS);
            setMaxAge(context.instance().getAttribute(DURATION));

            selector = new RadiusSelector<>(LivingEntity.class)
                    .setRange(radius);
        }

        @Override
        protected void onSpawn() {
            Location location = getLocation();
            World world = location.getWorld();

            TextColor textColor = getPrimarySpellType().textColor();

            Vector offset = new Vector(radius, 0, 0);

            int points = (int) (GLYPHS_PER_BLOCK * Math.PI * 2 * radius);
            double angleBetweenGlyphs = Math.TAU / points;

            location.setRotation(0, 0);

            float textScale = 1.5f;
            Vector3f scaleVector = new Vector3f(textScale, textScale, textScale);
            for (int i = 0; i < points; i++) {
                TextDisplay textDisplay = EffectUtils.getGlyphDisplay(
                        textColor,
                        location,
                        offset.toVector3f(),
                        scaleVector,
                        new AxisAngle4f((float) (i * angleBetweenGlyphs + (Math.PI / 2)), 0, 1, 0),
                        new AxisAngle4f((float) -(Math.PI / 2), 1, 0, 0)
                );
                displays.add(textDisplay);

                offset.rotateAroundY(angleBetweenGlyphs);
            }

            world.getPlayersSeeingChunk(location.getChunk()).forEach(player -> {
                displays.forEach(display -> {
                    PacketEventsWrapper.showFakeEntity(player, display);
                });
            });
        }

        @Override
        protected boolean tick() {
            if (Bukkit.getCurrentTick() % 5 == 0) {
                world.spawnParticle(Particle.SPORE_BLOSSOM_AIR, getLocation().add(new Vector(radius / 2, Math.random() * 4, 0).rotateAroundY(Math.random() * Math.TAU)), 1);
            }

            if (Bukkit.getCurrentTick() % 10 == 0) {
                List<LivingEntity> inCircle = selector.select(getLocation());

                inCircle.forEach(target -> {
                    healWithParticles(castContext, target);
                });
            }

            return !CastingManager.isConcentratingOn(castContext.player(), castContext);
        }

        @Override
        protected void onRemove() {
            Location location = getLocation();
            World world = location.getWorld();

            world.getPlayersSeeingChunk(location.getChunk()).forEach(player -> {
                displays.forEach(display -> {
                    PacketEventsWrapper.removeEntity(player, display);
                });
            });

            if (CastingManager.isConcentratingOn(castContext.player(), castContext)) {
                CastingManager.stopConcentrating(castContext.player());
            }
        }
    }
}
