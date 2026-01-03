package wbs.wandcraft.events;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.context.CastingManager;
import wbs.wandcraft.context.CastingQueue;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.extensions.DamageSpell;

@SuppressWarnings("unused")
public class SpellEvents implements Listener {
    private static final boolean DEBUG = false;
    private static void debug(String message) {
        if (DEBUG) {
            WbsWandcraft.getInstance().getLogger().info(message);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getPlayer();

        Player killer = event.getPlayer().getKiller();
        if (killer != null) {
            CastingQueue castingQueue = CastingManager.getCurrentContext(victim);
            if (castingQueue != null) {
                CastContext context = castingQueue.getCurrent();

                if (context != null) {
                    SpellDefinition currentDefinition = context.instance().getDefinition();

                    if (currentDefinition instanceof DamageSpell damageSpell) {
                        Component deathMessage = damageSpell.getDeathMessage(killer, victim);
                        if (deathMessage != null) {
                            event.deathMessage(deathMessage);
                        }
                    }
                }
            }
        }
    }
}
