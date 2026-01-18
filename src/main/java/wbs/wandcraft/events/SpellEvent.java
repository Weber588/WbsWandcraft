package wbs.wandcraft.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import wbs.wandcraft.context.CastContext;

public abstract class SpellEvent extends Event {
	private final Player caster;
	private final CastContext spell;

	protected SpellEvent(Player caster, CastContext context) {
		this.caster = caster;
		this.spell = context;
	}

	public CastContext getSpell() {
		return spell;
	}

	public Player getCaster() {
		return caster;
	}

}
