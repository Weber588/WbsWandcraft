package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.util.CustomPersistentDataTypes;

public interface EntityProjectileSpell extends AbstractProjectileSpell {
    SpellAttribute<EntityType> PROJECTILE_TYPE = new SpellAttribute<>(
            "projectile_type",
            new CustomPersistentDataTypes.PersistentEnumType<>(EntityType.class),
            EntityType.ARROW
    );

    default void setupEntityProjectile() {
        addAttribute(PROJECTILE_TYPE);
    }

    @Override
    default void cast(CastContext context) {
        Player player = context.player();
        SpellInstance instance = context.instance();

        Double speed = instance.getAttribute(SPEED);
        EntityType type = instance.getAttribute(PROJECTILE_TYPE);

        context.player().getWorld().spawnEntity(
                context.player().getEyeLocation(),
                type,
                CreatureSpawnEvent.SpawnReason.SPELL,
                projectile -> {
                    configure(projectile, context);
                    projectile.setVelocity(WbsEntityUtil.getFacingVector(player, speed));
                    if (projectile instanceof Fireball fireball) {
                        fireball.setDirection(projectile.getVelocity());
                    }
                }
        );
    }

    void configure(Entity projectile, CastContext context);
}
