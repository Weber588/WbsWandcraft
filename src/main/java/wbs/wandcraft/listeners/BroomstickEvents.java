package wbs.wandcraft.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import wbs.wandcraft.entities.Broomstick;
import wbs.wandcraft.wand.types.BroomstickWand;

import java.util.List;

import static wbs.wandcraft.entities.Broomstick.breakBroomstick;
import static wbs.wandcraft.entities.Broomstick.getBroomstickWand;

public class BroomstickEvents implements Listener {
    @EventHandler
    public void onBroomstickBreak(PlayerInteractEntityEvent event) {
        Entity check = getRootVehicle(event.getRightClicked());

        BroomstickWand wand = getBroomstickWand(check);
        if (wand == null) {
            return;
        }

        for (Entity passenger : check.getPassengers()) {
            if (passenger instanceof Interaction toRide) {
                if (toRide.getInteractionWidth() == 0) {
                    toRide.addPassenger(event.getPlayer());
                }
                return;
            }
        }
    }

    private static Entity getRootVehicle(Entity check) {
        while (check instanceof Interaction && check.getVehicle() != null) {
            check = check.getVehicle();
        }
        return check;
    }

    @EventHandler
    public void onBroomstickBreak(EntityDeathEvent event) {
        Entity check = getRootVehicle(event.getEntity());

        BroomstickWand wand = getBroomstickWand(check);
        if (wand == null) {
            return;
        }

        breakBroomstick(check, wand);
        event.setCancelled(true);
    }

    @EventHandler
    public void onBroomstickBreak(EntityDamageByEntityEvent event) {
        Entity check = getRootVehicle(event.getEntity());

        BroomstickWand wand = getBroomstickWand(check);
        if (wand == null) {
            return;
        }

        breakBroomstick(check, wand);
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityLoad(EntitiesLoadEvent event) {
        List<Entity> entities = event.getEntities();

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity) {
                BroomstickWand wand = getBroomstickWand(livingEntity);
                if (wand == null) {
                    continue;
                }

                new Broomstick(livingEntity);
            }
        }
    }
}
