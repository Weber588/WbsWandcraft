package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.context.CastingManager;
import wbs.wandcraft.effects.StatusEffectInstance;
import wbs.wandcraft.effects.StatusEffectManager;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.DurationalSpell;
import wbs.wandcraft.spell.definitions.extensions.SpeedSpell;

import java.util.UUID;

import static wbs.wandcraft.spell.definitions.type.SpellType.ENDER;
import static wbs.wandcraft.spell.definitions.type.SpellType.VOID;

public class CrowsCallSpell extends SpellDefinition implements CastableSpell, DurationalSpell, SpeedSpell {
    public CrowsCallSpell() {
        super("crows_call");

        addSpellType(ENDER);
        addSpellType(VOID);

        setAttribute(COST, 500);
        setAttribute(COOLDOWN, 7 * Ticks.TICKS_PER_SECOND);

        setAttribute(SPEED, 2d);
        setAttribute(DURATION, 30 * Ticks.TICKS_PER_SECOND);
    }

    @Override
    public String rawDescription() {
        return "Fly straight up, and glide until you touch the ground.";
    }

    @Override
    public void cast(CastContext context) {
        Player caster = context.player();
        SpellInstance instance = context.instance();

        if (StatusEffectManager.getInstance(caster, StatusEffectManager.GLIDING) != null) {
            WbsWandcraft.getInstance().sendActionBar("Already gliding!", caster);
            return;
        }

        if (caster.isFlying()) {
            caster.setFlying(false);
        }

        int duration = instance.getAttribute(DURATION);
        caster.getPersistentDataContainer().set(getKey(), PersistentDataType.INTEGER, duration);

        caster.setVelocity(caster.getVelocity().add(new Vector(0, instance.getAttribute(SPEED), 0)));

        UUID uuid = caster.getUniqueId();
        CastingManager.startConcentrating(caster, context);
        WbsWandcraft plugin = WbsWandcraft.getInstance();
        new BukkitRunnable() {
            @Override
            public void run() {
                Player updatedPlayer = Bukkit.getPlayer(uuid);
                if (updatedPlayer == null || !updatedPlayer.isOnline()) {
                    cancel();
                    return;
                }

                if (!CastingManager.isConcentrating(updatedPlayer, context)) {
                    cancel();
                    return;
                }

                // If they're falling, or already gliding, create/update effect
                if (updatedPlayer.getVelocity().getY() <= 0 || updatedPlayer.isGliding()) {
                    StatusEffectInstance.applyEffect(updatedPlayer, StatusEffectManager.GLIDING, duration, true, updatedPlayer);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    @Override
    public boolean requiresConcentration() {
        return true;
    }
}
