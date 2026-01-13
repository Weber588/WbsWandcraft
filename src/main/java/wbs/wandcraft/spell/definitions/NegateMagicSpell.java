package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.context.CastingManager;
import wbs.wandcraft.effects.StatusEffectInstance;
import wbs.wandcraft.effects.StatusEffectManager;
import wbs.wandcraft.objects.MagicObjectManager;
import wbs.wandcraft.objects.PersistenceLevel;
import wbs.wandcraft.objects.generics.MagicObject;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.RadiusedSpell;
import wbs.wandcraft.spell.definitions.type.SpellType;
import wbs.wandcraft.spellbook.Spellbook;
import wbs.wandcraft.wand.Wand;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class NegateMagicSpell extends SpellDefinition implements CastableSpell, RadiusedSpell {
    // Should this be a list of non-magic potion effects (hunger, weakness, poison etc.) since most are magic?
    private static final Set<PotionEffectType> MAGIC_EFFECTS = Set.of(
            PotionEffectType.HASTE,
            PotionEffectType.FIRE_RESISTANCE,
            PotionEffectType.MINING_FATIGUE,
            PotionEffectType.SPEED,
            PotionEffectType.SLOWNESS,
            PotionEffectType.STRENGTH,
            PotionEffectType.JUMP_BOOST,
            PotionEffectType.REGENERATION,
            PotionEffectType.WATER_BREATHING,
            PotionEffectType.INVISIBILITY,
            PotionEffectType.NIGHT_VISION,
            PotionEffectType.WITHER,
            PotionEffectType.HEALTH_BOOST,
            PotionEffectType.ABSORPTION,
            PotionEffectType.GLOWING,
            PotionEffectType.LEVITATION,
            PotionEffectType.LUCK,
            PotionEffectType.UNLUCK,
            PotionEffectType.SLOW_FALLING,
            PotionEffectType.CONDUIT_POWER,
            PotionEffectType.DARKNESS,
            PotionEffectType.WIND_CHARGED,
            PotionEffectType.WEAVING,
            PotionEffectType.OOZING,
            PotionEffectType.INFESTED
    );

    public NegateMagicSpell() {
        super("negate_magic");

        addSpellType(SpellType.SCULK);
        addSpellType(SpellType.VOID);

        setAttribute(COST, 500);
        setAttribute(COOLDOWN, 30 * Ticks.TICKS_PER_SECOND);

        setAttribute(RADIUS, 50d);
    }

    @Override
    public void cast(CastContext context) {
        double radius = context.instance().getAttribute(RADIUS);

        Location location = context.location();

        List<MagicObject> nearbyObjects = MagicObjectManager.getNearbyActive(location, radius);
        for (MagicObject magicObject : nearbyObjects) {
            magicObject.dispel(PersistenceLevel.STRONG);
        }

        Collection<Entity> nearbyEntities = location.getNearbyEntities(radius, radius, radius);

        location.getWorld().spawnParticle(
                Particle.END_ROD,
                location,
                (int) (radius * radius),
                radius / 2,
                radius / 2,
                radius / 2,
                0.05
        );

        Component concentrationMessage = Component.text("Concentration broken!").color(NamedTextColor.RED);
        Component castingMessage = Component.text("Spell interrupted!").color(NamedTextColor.RED);

        for (Entity nearbyEntity : nearbyEntities) {
            if (nearbyEntity.getLocation().distanceSquared(location) > radius * radius) {
                continue;
            }

            if (nearbyEntity instanceof LivingEntity livingEntity) {
                Collection<StatusEffectInstance> instances = StatusEffectManager.getInstances(livingEntity);
                instances.forEach(instance -> instance.cancel(true));

                MAGIC_EFFECTS.forEach(livingEntity::removePotionEffect);

                if (livingEntity instanceof Spellcaster spellcaster) {
                    spellcaster.setSpell(Spellcaster.Spell.NONE);
                }
                if (livingEntity instanceof Guardian guardian) {
                    guardian.setLaser(false);
                }
                if (livingEntity instanceof Creaking creaking) {
                    creaking.setHealth(0);
                }

                if (CastingManager.isConcentrating(livingEntity)) {
                    livingEntity.sendActionBar(concentrationMessage);
                    CastingManager.stopConcentrating(livingEntity);
                }
                if (CastingManager.isCasting(livingEntity)) {
                    livingEntity.sendActionBar(castingMessage);
                    CastingManager.stopCasting(livingEntity);
                }
                ItemStack activeItem = livingEntity.getActiveItem();
                if (Spellbook.isSpellbook(activeItem) || Wand.fromItem(activeItem) != null) {
                    livingEntity.clearActiveItem();
                }
            }

            if (nearbyEntity.getEntitySpawnReason() == CreatureSpawnEvent.SpawnReason.SPELL) {
                nearbyEntity.remove();
            }
        }
    }

    @Override
    public String rawDescription() {
        return "Dispels all magic effects in a radius around you.";
    }
}
