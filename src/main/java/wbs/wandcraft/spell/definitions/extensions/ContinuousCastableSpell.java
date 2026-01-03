package wbs.wandcraft.spell.definitions.extensions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.cost.CostUtils;
import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;

public interface ContinuousCastableSpell extends CastableSpell {
    SpellAttribute<Integer> FIXED_DURATION = new IntegerSpellAttribute("fixed_duration", Ticks.TICKS_PER_SECOND)
            .setShowAttribute(duration -> duration != 20);
    SpellAttribute<Integer> MAX_DURATION = new IntegerSpellAttribute("max_duration", 5 * Ticks.TICKS_PER_SECOND)
            .setShowAttribute(duration -> duration > 0);
    SpellAttribute<Integer> COST_PER_TICK = new IntegerSpellAttribute("cost_per_tick", 5)
            .setShowAttribute(cost -> cost > 0);

    default void setupContinuousCast() {
        addAttribute(MAX_DURATION);
        addAttribute(COST_PER_TICK);
        setAttribute(COST, 25);
    }

    default void cast(CastContext context) {
        WbsWandcraft plugin = WbsWandcraft.getInstance();

        onStartCasting(context);

        Player player = context.player();
        boolean isContinuousCast = player.isSneaking();
        int startTick = Bukkit.getCurrentTick();

        int endTick;
        if (isContinuousCast) {
            endTick = startTick + context.instance().getAttribute(MAX_DURATION);
        } else {
            Component continuousCastPrompt = Component.text("Hold ").color(NamedTextColor.LIGHT_PURPLE)
                    .append(Component.keybind("key.sneak").color(NamedTextColor.AQUA))
                    .append(Component.text(" to continuous cast!"));
            player.sendActionBar(continuousCastPrompt);
            endTick = startTick + context.instance().getAttribute(FIXED_DURATION);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = context.getOnlinePlayer();
                if (endTick <= Bukkit.getCurrentTick() || player == null || isContinuousCast && !player.isSneaking() || !player.isOnline() || player.isDead()) {
                    cancel();
                    return;
                }

                tick(context, Bukkit.getCurrentTick() - startTick, endTick - Bukkit.getCurrentTick());
                CostUtils.takeCost(player, context.instance().getAttribute(COST_PER_TICK));
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
