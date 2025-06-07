package wbs.wandcraft.effects;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Keyed;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface StatusEffect extends Keyed {
    GlidingStatusEffect GLIDING = new GlidingStatusEffect();
    StunnedEffect STUNNED = new StunnedEffect();

    Component display();
    default BossBar.Color barColour() {
        return BossBar.Color.YELLOW;
    }
    default BossBar.Overlay barStyle() {
        return BossBar.Overlay.PROGRESS;
    }
    boolean tick(LivingEntity entity, int timeLeft);
    default boolean isValid(LivingEntity entity) {
        return true;
    }
    default void registerEvents() {}
}
