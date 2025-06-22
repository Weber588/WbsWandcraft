package wbs.wandcraft.objects.generics;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import wbs.wandcraft.events.objects.MagicObjectMoveEvent;
import wbs.wandcraft.objects.colliders.Collider;
import wbs.wandcraft.objects.colliders.Collision;
import wbs.wandcraft.context.CastContext;

public abstract class KinematicMagicObject extends MagicObject {
	
	public Location location;

	public KinematicMagicObject(Location location, Player caster, CastContext context) {
		super(location, caster, context);
		this.location = location;
	}
	
	@Override
	public Location getLocation() {
		return location.clone();
	}

	/**
	 * Change the location of this object without
	 * triggering a move event
	 * @param location The new location
	 */
	public void setLocation(Location location) {
		this.location = location;

		if (collider == null || collider.getWorld() != world) return;
		Vector offset = collider.getLocation().subtract(getLocation()).toVector();

		collider.setLocation(location.clone().add(offset));
	}

	/**
	 * Attempts to move this object from it's current location
	 * to the provided one, and returns the actual location
	 * it was moved to after an event is fired.
	 * @param location The location to move to
	 * @return The new location
	 */
	public Location move(Location location) {
		MagicObjectMoveEvent moveEvent = new MagicObjectMoveEvent(this, location);

		for (MagicObject object : Collider.getObjectsWithColliders()) {
			if (object == this) continue;
			object.collider.tryColliding(moveEvent);
			if (moveEvent.isCancelled()) return getLocation();
		}

		Bukkit.getPluginManager().callEvent(moveEvent);

		if (moveEvent.isCancelled()) return getLocation();

		Location newLocation = moveEvent.getNewLocation();

		setLocation(newLocation);

		return newLocation;
	}

    public void onCollide(MagicObjectMoveEvent moveEvent, Collision collision) {

	}
}
