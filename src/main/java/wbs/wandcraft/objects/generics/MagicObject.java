package wbs.wandcraft.objects.generics;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.WbsMath;
import wbs.utils.util.particles.WbsParticleGroup;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.events.objects.MagicObjectSpawnEvent;
import wbs.wandcraft.exceptions.MagicObjectExistsException;
import wbs.wandcraft.objects.MagicObjectManager;
import wbs.wandcraft.objects.PersistenceLevel;
import wbs.wandcraft.objects.colliders.Collider;

import java.util.Objects;

public abstract class MagicObject {
	public Location spawnLocation; // The spawn location; should never change. To move, use DynamicMagicObject
	public Player caster;
	@NotNull
	public SpellInstance castingSpell;
	
	public MagicObject(Location location, Player caster, @NotNull SpellInstance castingSpell) {
		this.spawnLocation = location;
		this.caster = caster;
		this.castingSpell = castingSpell;
		world = Objects.requireNonNull(location.getWorld());

		MagicObjectManager.registerMagicObject(this);
	}

	public World world;
	protected boolean active = true;
	protected boolean isPersistent = false; // Persistent objects are immune to some removal effects (such as Negate Magic)

	protected PersistenceLevel persistenceLevel = PersistenceLevel.WEAK;

	private int age = 0;

	private int maxAge = Integer.MAX_VALUE;

	protected int timerID = -1; // The ID of the runnable

	protected Collider collider;

	@Nullable
	protected WbsParticleGroup effects = null; // The vast majority of magicobjects will use particles
	@Nullable
	protected WbsParticleGroup endEffects = null;
	@Nullable
	protected WbsParticleGroup dispelEffects = null;

	protected boolean debug = false;

	protected void debug(String message) {
		if (this.debug) {
			WbsWandcraft.getInstance().getLogger().info("MagicObject DEBUG|" + Integer.toHexString(timerID) + "| " + message);
		}
	}

	public void spawn() {
		if (timerID != -1) {
			throw new MagicObjectExistsException();
		}

		MagicObjectSpawnEvent spawnEvent = new MagicObjectSpawnEvent(this);

		Bukkit.getPluginManager().callEvent(spawnEvent);

		if (spawnEvent.isCancelled()) {
			return;
		}

		onRun();

		timerID = new BukkitRunnable() {
			boolean cancel = false;
			@Override
	        public void run() {
				debug("Tick started");
				cancel = tick();

				if (!cancel && effects != null) {
					effects.play(getLocation());
				}

				age++;

				if (cancel || !active || age >= maxAge) {
					if (cancel) {
						debug("Cancelled");
					}
					if (!active) {
						debug("Made inactive");
					}
					if (age >= maxAge) {
						debug("Max age hit");
						onMaxAgeHit();
					}
					debug("Removing");
					remove();
				}
	        }
	    }.runTaskTimer(WbsWandcraft.getInstance(), 0L, 1L).getTaskId();

		debug("Spawned");
	}

	protected void onMaxAgeHit() {

	}

	/**
	 * Called when the object starts running, before the timer is scheduled
	 */
	protected void onRun() {

	}

	/**
	 * Called every tick by the magic object
	 * @return Whether or not to cancel. True to make the object expire.
	 */
	protected abstract boolean tick();

	public final void remove() {
		remove(false);
	}

	/**
	 * Remove this magic object.
	 * @param force If the object is persistent, this must be true to remove it.
	 * @return Whether or not the object was removed.
	 */
	public final boolean remove(boolean force) {
		if (isPersistent && !force) return false;
		if (!active) return false;
		
	//	plugin.broadcast("Fizzling ID " + timerID);
		
		active = false;
		MagicObjectManager.remove(caster.getUniqueId(), this);
		if (timerID != -1) {
			Bukkit.getScheduler().cancelTask(timerID);
		}
		
		if (endEffects != null) {
			endEffects.play(getLocation());
		}

		if (collider != null) {
			collider.remove();
		}

		onRemove();

		return true;
	}

	/**
	 * Called after this entity is removed and everything else is resolved (including deregistration)
	 */
	protected void onRemove() {

	}

	public boolean dispel(PersistenceLevel persistenceLevel) {
		boolean remove = this.persistenceLevel.ordinal() <= persistenceLevel.ordinal();

		boolean removed = false;
		if (remove) {
			removed = remove(false);
		}

		return removed;
	}

	/**
	 * Returns the distance between this object and another magic object.
	 * Returns {@link Double#POSITIVE_INFINITY} if it's in another world.
	 * @param other The object to measure distance to.
	 * @return The distance between the objects, or {@link Double#POSITIVE_INFINITY}
	 * if they're in different worlds.
	 */
	public double distance(MagicObject other) {
		Location otherLoc = other.getLocation();
		if (otherLoc.getWorld() != world) return Double.POSITIVE_INFINITY;

		return otherLoc.distance(getLocation());
	}

	public boolean isExpired() {
		return !active;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isPersistent() {
		return isPersistent;
	}
	public void setPersistent(boolean isPersistent) {
		this.isPersistent = isPersistent;
	}

	public MagicObject setParticle(WbsParticleGroup effects) {
		this.effects = effects.clone();
		return this;
	}
	
	public MagicObject setEndEffects(WbsParticleGroup endEffects) {
		this.endEffects = endEffects.clone();
		return this;
	}

	public final Location getSpawnLocation() {
		return spawnLocation.clone();
	}
	
	// This method can be overridden by extending classes
	public Location getLocation() {
		return spawnLocation.clone();
	}

	//************
	// Math methods
	protected Vector scaleVector(Vector original, double magnitude) {
		return WbsMath.scaleVector(original, magnitude);
	}

	protected boolean chance(double percent) {
		return WbsMath.chance(percent);
	}
	
	protected Vector randomVector(double magnitude) {
		return WbsMath.randomVector(magnitude);
	}

	@NotNull
	public SpellInstance getSpell() {
		return castingSpell;
	}

	public Player getCaster() {
		return caster;
	}

	public int getAge() {
		return age;
	}

	public int getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(int maxAge) {
		this.maxAge = maxAge;
	}

	public PersistenceLevel getPersistenceLevel() {
		return persistenceLevel;
	}

	public void setPersistenceLevel(PersistenceLevel persistenceLevel) {
		this.persistenceLevel = persistenceLevel;
	}

	@Nullable
	public Collider getCollider() {
		return collider;
	}

	public void setCollider(@Nullable Collider collider) {
		this.collider = collider;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
