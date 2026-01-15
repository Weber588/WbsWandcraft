package wbs.wandcraft.spell.definitions;

import com.destroystokyo.paper.entity.ai.*;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.ai.CustomAvoidGoal;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.effects.PolymorphedEffect;
import wbs.wandcraft.effects.StatusEffectInstance;
import wbs.wandcraft.effects.StatusEffectManager;
import wbs.wandcraft.spell.RequiresPlugin;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.DurationalSpell;
import wbs.wandcraft.spell.definitions.type.SpellType;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

@RequiresPlugin("LibsDisguises")
@NullMarked
public class HallucinationSpell extends SpellDefinition implements CastableSpell, DurationalSpell {
    public HallucinationSpell() {
        super("hallucination");

        addSpellType(SpellType.SCULK);
        addSpellType(SpellType.NATURE);

        setAttribute(COST, 350);
        setAttribute(COOLDOWN, 30 * Ticks.TICKS_PER_SECOND);

        setAttribute(DURATION, 15 * Ticks.TICKS_PER_SECOND);
    }

    @Override
    public void cast(CastContext context) {
        Player player = context.player();
        Location location = player.getLocation();

        Pillager spawnedMob = location.getWorld().spawn(location, Pillager.class, CreatureSpawnEvent.SpawnReason.SPELL, preSpawnedMob -> {
            preSpawnedMob.getEquipment().clear();

            preSpawnedMob.setSilent(true);
            PlayerDisguise disguise = new PlayerDisguise(player);
            disguise.setEntity(preSpawnedMob);

            PlayerWatcher watcher = disguise.getWatcher();
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (player.canUseEquipmentSlot(slot)) {
                    watcher.setItemStack(slot, player.getInventory().getItem(slot));
                    watcher.setSprinting(true);
                }
            }

            disguise.startDisguise();

            MobGoals mobGoals = Bukkit.getMobGoals();

            Collection<Goal<@NotNull Pillager>> goals = mobGoals.getAllGoals(preSpawnedMob);
            for (Goal<@NotNull Pillager> goal : goals) {
                if (!goal.getKey().equals(VanillaGoal.FLOAT)) {
                    mobGoals.removeGoal(preSpawnedMob, goal);
                }
            }

            mobGoals.addGoal(preSpawnedMob, 2, new CustomAvoidGoal(preSpawnedMob, 1.3));
            mobGoals.addGoal(preSpawnedMob, 3, new AlwaysJumpGoal(preSpawnedMob));

            AttributeInstance movementAttribute = Objects.requireNonNull(preSpawnedMob.getAttribute(Attribute.MOVEMENT_SPEED));
            movementAttribute.setBaseValue(0.5);

            AttributeInstance armorAttribute = Objects.requireNonNull(preSpawnedMob.getAttribute(Attribute.ARMOR));
            armorAttribute.setBaseValue(Objects.requireNonNull(player.getAttribute(Attribute.ARMOR)).getValue());

            AttributeInstance waterMovementAttribute = Objects.requireNonNull(preSpawnedMob.getAttribute(Attribute.WATER_MOVEMENT_EFFICIENCY));
            waterMovementAttribute.setBaseValue(0.25);

            AttributeInstance healthAttribute = Objects.requireNonNull(preSpawnedMob.getAttribute(Attribute.MAX_HEALTH));
            double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue();
            healthAttribute.setBaseValue(maxHealth);
            preSpawnedMob.setHealth(maxHealth);

            preSpawnedMob.setPersistent(false);
        });

        int duration = context.instance().getAttribute(DURATION);

        StatusEffectInstance.applyEffect(player, StatusEffectManager.INVISIBLE, duration, false, player);

        List<Entity> nearbyEntities = player.getNearbyEntities(100, 100, 100);
        for (Entity nearbyEntity : nearbyEntities) {
            if (nearbyEntity instanceof Mob mob) {
                if (player.equals(mob.getTarget())) {
                    mob.setTarget(spawnedMob);
                }
            }
        }

        WbsWandcraft.getInstance().runLater(() -> {
            PolymorphedEffect.POLYMORPH_EFFECT.play(Particle.CLOUD, player.getLocation().add(0, player.getHeight() / 2, 0));
            if (spawnedMob.isValid()) {
                PolymorphedEffect.POLYMORPH_EFFECT.play(Particle.CLOUD, spawnedMob.getLocation().add(0, player.getHeight() / 2, 0));
                spawnedMob.remove();
            }
        }, duration);
    }

    @Override
    public String rawDescription() {
        return "Turn invisible and leave behind a hallucination of yourself that flees from nearby mobs, allowing you to make a quick getaway";
    }

    private static class AlwaysJumpGoal implements Goal<Mob> {
        private final Mob mob;

        private AlwaysJumpGoal(Mob mob) {
            this.mob = mob;
        }

        @Override
        public boolean shouldActivate() {
            return mob.isOnGround();
        }

        @Override
        public void tick() {
            mob.setJumping(true);
        }

        @Override
        public GoalKey<Mob> getKey() {
            return GoalKey.of(Mob.class, WbsWandcraft.getKey("always_jump"));
        }

        @Override
        public EnumSet<GoalType> getTypes() {
            return EnumSet.of(GoalType.JUMP);
        }
    }
}
