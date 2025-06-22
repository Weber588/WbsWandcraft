package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.effects.StatusEffectInstance;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.DurationalSpell;
import wbs.wandcraft.spell.definitions.extensions.SpeedSpell;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CrowsCallSpell extends SpellDefinition implements CastableSpell, DurationalSpell, SpeedSpell {
    private static final Map<UUID, Integer> CURRENT_TIMERS = new HashMap<>();

    public CrowsCallSpell() {
        super("crows_call");

        setAttribute(SPEED, 2d);
        setAttribute(DURATION, 30 * Ticks.TICKS_PER_SECOND);
    }

    @Override
    public Component description() {
        return Component.text("Fly straight up, and glide until you touch the ground.");
    }

    @Override
    public void cast(CastContext context) {
        Player caster = context.player();
        SpellInstance instance = context.instance();

        if (caster.isFlying()) {
            caster.setFlying(false);
        }

        int duration = instance.getAttribute(DURATION);
        caster.getPersistentDataContainer().set(getKey(), PersistentDataType.INTEGER, duration);

        caster.setVelocity(caster.getVelocity().add(new Vector(0, instance.getAttribute(SPEED), 0)));

        WbsWandcraft plugin = WbsWandcraft.getInstance();
        new BukkitRunnable() {
            @Override
            public void run() {
                Player updatedPlayer = Bukkit.getPlayer(caster.getUniqueId());
                // If the player is online and falling, start the glide timer
                if (updatedPlayer != null && updatedPlayer.isOnline()) {
                    // If they're falling, or already gliding, create/update effect
                    if (updatedPlayer.getVelocity().getY() <= 0 || updatedPlayer.isGliding()) {
                        StatusEffectInstance.applyEffect(updatedPlayer, StatusEffect.GLIDING, duration, true, updatedPlayer);
                        cancel();
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }
}
