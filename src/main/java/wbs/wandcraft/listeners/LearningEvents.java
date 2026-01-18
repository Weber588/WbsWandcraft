package wbs.wandcraft.listeners;

import com.google.common.collect.Multimap;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.learning.LearningMethod;
import wbs.wandcraft.learning.LearningTrigger;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spellbook.Spellbook;

import java.util.LinkedList;
import java.util.List;

@NullMarked
public class LearningEvents implements Listener {
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Multimap<SpellDefinition, LearningMethod> learningMap = WbsWandcraft.getInstance().getSettings().getLearningMap();

        Player player = event.getPlayer();

        List<SpellDefinition> spells = new LinkedList<>();
        learningMap.forEach((spell, method) -> {
            if (method instanceof LearningTrigger<?> trigger) {
                if (trigger.shouldGrantOnLogin(player)) {
                    spells.add(spell);
                }
            }
        });

        Spellbook.teachSpells(player, spells);
    }

    @EventHandler
    public void onGainAdvancement(PlayerAdvancementDoneEvent event) {
        teachIfMatch(event);
    }

    @EventHandler
    public void onKill(EntityDeathEvent event) {
        teachIfMatch(event);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        teachIfMatch(event);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) {
            return;
        }

        teachIfMatch(event);
    }

    private static <T extends Event> void teachIfMatch(T event) {
        Multimap<SpellDefinition, LearningMethod> learningMap = WbsWandcraft.getInstance().getSettings().getLearningMap();

        learningMap.forEach((spell, method) -> {
            if (method instanceof LearningTrigger<?> trigger) {
                if (trigger.getMatchClass().isAssignableFrom(event.getClass())) {
                    //noinspection unchecked
                    LearningTrigger<T> typedTrigger = (LearningTrigger<T>) trigger;

                    Player player = typedTrigger.getPlayer(event);
                    if (player != null) {
                        if (typedTrigger.matches(event)) {
                            Spellbook.teachSpell(player, spell);
                        }
                    }
                }
            }
        });
    }

}
