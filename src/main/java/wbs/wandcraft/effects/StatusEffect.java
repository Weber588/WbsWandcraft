package wbs.wandcraft.effects;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Keyed;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.WbsKeyed;

@NullMarked
public interface StatusEffect extends Keyed {
    GlidingStatusEffect GLIDING = new GlidingStatusEffect();
    StunnedEffect STUNNED = new StunnedEffect();
    PlanarBindingEffect PLANAR_BINDING = new PlanarBindingEffect();
    DeathWalkEffect DEATH_WALK = new DeathWalkEffect();

    default Component display() {
        return Component.text(WbsKeyed.toPrettyString(this)).color(NamedTextColor.YELLOW);
    }
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
