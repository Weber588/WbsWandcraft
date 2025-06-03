package wbs.wandcraft.effects;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface StatusEffect extends Keyed {
    GlidingStatusEffect GLIDING = new GlidingStatusEffect();

    Component display();
    default BossBar.Color barColour() {
        return BossBar.Color.BLUE;
    }
    default BossBar.Overlay barStyle() {
        return BossBar.Overlay.PROGRESS;
    }
    boolean tick(Player player, int timeLeft);
    boolean isValid(Player player);
    default void registerEvents() {}
}
