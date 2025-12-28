package wbs.wandcraft.objects;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import wbs.wandcraft.objects.colliders.MagicSpawnedBlock;
import wbs.wandcraft.objects.generics.MagicEntityEffect;
import wbs.wandcraft.objects.generics.MagicObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class MagicObjectManager {
    private static final Multimap<UUID, MagicObject> activeObjects = HashMultimap.create();
    public static Collection<MagicObject> getAllActive() {
        return activeObjects.values();
    }

    public static Collection<MagicObject> getAllActive(Player player) {
        return activeObjects.get(player.getUniqueId());
    }

    public static <T extends MagicObject> Collection<T> getAllActive(Player player, Class<T> clazz) {
        return activeObjects.get(player.getUniqueId()).stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .toList();
    }
    public static <T extends MagicObject> Collection<T> getAllActive(Class<T> clazz) {
        return getAllActive().stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .toList();
    }

    public static Collection<MagicSpawnedBlock> getAllActive(Block block) {
        return MagicObjectManager.getAllActive(MagicSpawnedBlock.class).stream()
                .filter(magicSpawnedBlock -> magicSpawnedBlock.getBlock().equals(block))
                .toList();
    }

    public static List<MagicObject> getNearbyActive(Location location, double distance) {
        List<MagicObject> nearby = new LinkedList<>();
        double distanceSquared = distance * distance;
        for (MagicObject object : activeObjects.values()) {
            Location objLocation = object.getLocation();
            if (!object.world.equals(location.getWorld())) continue;
            if (objLocation.distanceSquared(location) <= distanceSquared) {
                nearby.add(object);
            }
        }
        return nearby;
    }

    public static List<MagicObject> getNearbyActive(Location location, double distance, Player player) {
        List<MagicObject> nearby = new LinkedList<>();
        for (MagicObject object : activeObjects.get(player.getUniqueId())) {
            if (object.getLocation().distance(location) <= distance) {
                nearby.add(object);
            }
        }
        return nearby;
    }

    public static List<MagicEntityEffect> getActiveEffects(Entity entity) {
        List<MagicEntityEffect> effects = new LinkedList<>();

        for (MagicObject obj : activeObjects.values()) {
            if (obj instanceof MagicEntityEffect effect) {
                if (effect.getEntity().equals(entity)) {
                    effects.add(effect);
                }
            }
        }

        return effects;
    }

    public static void registerMagicObject(MagicObject object) {
        activeObjects.put(object.caster.getUniqueId(), object);
    }

    public static void remove(UUID uniqueId, MagicObject magicObject) {
        activeObjects.remove(uniqueId, magicObject);
    }
}
