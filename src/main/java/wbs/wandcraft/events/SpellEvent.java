package wbs.wandcraft.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import wbs.wandcraft.spell.definitions.SpellInstance;

public abstract class SpellEvent extends Event {
	private final Player caster;
	private final SpellInstance spell;

	protected SpellEvent(Player caster, SpellInstance spell) {
		this.caster = caster;
		this.spell = spell;
	}
	
	public SpellInstance getSpell() {
		return spell;
	}

	public Player getCaster() {
		return caster;
	}

}
