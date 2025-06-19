package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.wandcraft.WbsWandcraft;

public interface ContinuousCastableSpell extends CastableSpell, DurationalSpell {
    default void cast(CastContext context) {
        WbsWandcraft plugin = WbsWandcraft.getInstance();

        onStartCasting(context);

        Player player = context.player();
        boolean startedSneaking = player.isSneaking();
        int startTick = Bukkit.getCurrentTick();

        int endTick = startTick + context.instance().getAttribute(DURATION);

        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = Bukkit.getPlayer(context.player().getUniqueId());
                if (endTick <= Bukkit.getCurrentTick() || player == null || startedSneaking && !player.isSneaking() || !player.isOnline() || player.isDead()) {
                    cancel();
                    return;
                }

                tick(context, Bukkit.getCurrentTick() - startTick, endTick - Bukkit.getCurrentTick());
            }

            @Override
            public synchronized void cancel() {
                super.cancel();
                onStopCasting(context);
                context.finish();
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    default boolean completeAfterCast() {
        return false;
    }

    void onStartCasting(CastContext context);
    void tick(CastContext context, int tick, int ticksLeft);
    void onStopCasting(CastContext context);
}
