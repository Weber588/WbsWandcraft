package wbs.wandcraft.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import wbs.wandcraft.context.CastContext;

public abstract class SpellEvent extends Event {
	private final Player caster;
	private final CastContext spell;

	protected SpellEvent(Player caster, CastContext spell) {
		this.caster = caster;
		this.spell = spell;
	}
	
	public CastContext getSpell() {
		return spell;
	}

	public Player getCaster() {
		return caster;
	}

}
