package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.entities.selector.RadiusSelector;
import wbs.utils.util.pluginhooks.hooks.PacketEventsWrapper;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.context.CastingManager;
import wbs.wandcraft.cost.PlayerMana;
import wbs.wandcraft.objects.generics.MagicObject;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.DurationalSpell;
import wbs.wandcraft.spell.definitions.extensions.RadiusedSpell;
import wbs.wandcraft.spell.definitions.type.SpellType;
import wbs.wandcraft.util.EffectUtils;

import java.util.LinkedList;
import java.util.List;

public class ManaCircleSpell extends SpellDefinition implements CastableSpell, RadiusedSpell, DurationalSpell {
    private static final int GLYPHS_PER_BLOCK = 3;
    private static final int MANA_PER_TICK = 3;

    public ManaCircleSpell() {
        super("mana_circle");

        addSpellType(SpellType.ARCANE);

        setAttribute(COST, 500);
        setAttribute(COOLDOWN, 60 * Ticks.TICKS_PER_SECOND);

        setAttribute(RADIUS, 2d);
        setAttribute(DURATION, 15 * Ticks.TICKS_PER_SECOND);
    }

    @Override
    public void cast(CastContext context) {
        ManaCircleObject object = new ManaCircleObject(context.player().getLocation(), context);

        CastingManager.startConcentrating(context.player(), context);
        object.spawn();
    }

    @Override
    public String rawDescription() {
        return "Create a circle of runes that regenerates mana and magic attacks.";
    }

    private class ManaCircleObject extends MagicObject {
        private final List<TextDisplay> displays = new LinkedList<>();
        private final double radius;
        private final RadiusSelector<LivingEntity> selector;

        public ManaCircleObject(Location location, @NotNull CastContext context) {
            super(location.setRotation(0, 0), context);

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
                    PacketEventsWrapper.get().ifPresentOrElse(pe -> {
                        pe.showFakeEntity(display, player);
                    }, () -> {
                        world.addEntity(display);
                    });
                });
            });
        }

        @Override
        protected boolean tick() {
            if (Bukkit.getCurrentTick() % 5 == 0) {
                world.spawnParticle(Particle.ENCHANT, getLocation().add(new Vector(radius / 2, Math.random() * 4, 0).rotateAroundY(Math.random() * Math.TAU)), 1);
            }

            List<LivingEntity> inCircle = selector.select(getLocation());

            for (LivingEntity target : inCircle) {
                if (target instanceof Player player) {
                    new PlayerMana(player)
                            .addMana(MANA_PER_TICK)
                            .saveTo(player);
                } else if (target instanceof Spellcaster spellcaster) {
                    beginCasting(spellcaster);
                } else if (target instanceof Guardian guardian) {
                    guardian.setLaserTicks(guardian.getLaserTicks() + 1);
                } else if (target instanceof Vex vex) {
                    vex.heal(1, EntityRegainHealthEvent.RegainReason.MAGIC_REGEN);
                }
            }

            return !CastingManager.isConcentrating(context.player(), context);
        }

        private static void beginCasting(Spellcaster spellcaster) {
            List<Spellcaster.Spell> spells;
            if (spellcaster.getSpell() == Spellcaster.Spell.NONE) {
                if (spellcaster instanceof Evoker) {
                    spells = List.of(
                            Spellcaster.Spell.FANGS,
                            Spellcaster.Spell.SUMMON_VEX,
                            Spellcaster.Spell.WOLOLO
                    );
                } else if (spellcaster instanceof Illusioner) {
                    spells = List.of(
                            Spellcaster.Spell.BLINDNESS,
                            Spellcaster.Spell.DISAPPEAR
                    );
                } else {
                    return;
                }
                spellcaster.setSpell(WbsCollectionUtil.getRandom(spells));
            }
        }

        @Override
        protected void onRemove() {
            Location location = getLocation();
            World world = location.getWorld();

            world.getPlayersSeeingChunk(location.getChunk()).forEach(player -> {
                displays.forEach(display -> {

                    PacketEventsWrapper.get().ifPresentOrElse(pe -> {
                        pe.removeEntity(display, player);
                    }, display::remove);
                });
            });

            CastingManager.stopConcentrating(context.player(), context);
        }
    }
}
