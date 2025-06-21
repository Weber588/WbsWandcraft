package wbs.wandcraft.spell.definitions.extensions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.cost.CostUtils;

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
                Player player = context.getOnlinePlayer();
                if (endTick <= Bukkit.getCurrentTick() || player == null || startedSneaking && !player.isSneaking() || !player.isOnline() || player.isDead()) {
                    cancel();
                    return;
                }

                tick(context, Bukkit.getCurrentTick() - startTick, endTick - Bukkit.getCurrentTick());
                CostUtils.takeCost(player, context.instance().getAttribute(COST));
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

    default void onStartCasting(CastContext context) {}
    void tick(CastContext context, int tick, int ticksLeft);
    default void onStopCasting(CastContext context) {}
}
