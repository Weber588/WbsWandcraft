package wbs.wandcraft.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import wbs.wandcraft.wand.types.BroomstickWand;

import java.util.List;

public class BroomstickEvents implements Listener {
    @EventHandler
    public void onBroomstickBreak(PlayerInteractEntityEvent event) {
        Entity broomstick = event.getRightClicked();
        if (broomstick instanceof Interaction) {
            broomstick = broomstick.getVehicle();
            if (broomstick == null) {
                return;
            }
        }

        BroomstickWand wand = BroomstickWand.getBroomstickWand(broomstick);
        if (wand == null) {
            return;
        }

        for (Entity passenger : broomstick.getPassengers()) {
            if (passenger instanceof ItemDisplay display) {
                display.addPassenger(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onBroomstickBreak(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Interaction) {
            entity = entity.getVehicle();
            if (entity == null) {
                return;
            }
        }

        BroomstickWand wand = BroomstickWand.getBroomstickWand(entity);
        if (wand == null) {
            return;
        }

        BroomstickWand.breakBroomstick(entity, wand);
        event.setCancelled(true);
    }

    @EventHandler
    public void onBroomstickBreak(EntityDamageByEntityEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Interaction) {
            entity = entity.getVehicle();
            if (entity == null) {
                return;
            }
        }

        BroomstickWand wand = BroomstickWand.getBroomstickWand(entity);
        if (wand == null) {
            return;
        }

        BroomstickWand.breakBroomstick(entity, wand);
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityLoad(EntitiesLoadEvent event) {
        List<Entity> entities = event.getEntities();

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity) {
                BroomstickWand wand = BroomstickWand.getBroomstickWand(livingEntity);
                if (wand == null) {
                    continue;
                }

                BroomstickWand.startBroomstickTimer(livingEntity);
            }
        }
    }
}
