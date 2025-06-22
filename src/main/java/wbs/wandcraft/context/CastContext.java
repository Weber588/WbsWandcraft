package wbs.wandcraft.context;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.event.SpellTriggeredEvent;

import java.util.Objects;

@NullMarked
public final class CastContext {
    private final Player player;
    private final SpellInstance instance;
    private final Location location;
    private final @Nullable CastContext parent;
    private final @Nullable Runnable finishCallback;
    private final @Nullable Runnable failCallback;
    private boolean hasFinished = false;

    public CastContext(Player player, SpellInstance instance, Location location, @Nullable CastContext parent, @Nullable Runnable finishCallback, @Nullable Runnable failCallback) {
        this.player = player;
        this.instance = instance;
        this.location = location;
        this.parent = parent;
        this.finishCallback = finishCallback;
        this.failCallback = failCallback;
    }

    public CastContext(Player player, SpellInstance instance, Location location, @Nullable CastContext parent, @Nullable Runnable finishCallback) {
        this(player, instance, location, parent, finishCallback, null);
    }

    public void cast() {
        instance.cast(this);
    }

    public void cast(SpellInstance other, Location source) {
        other.cast(new CastContext(player, other, source, parent, null, null));
    }

    public <T> void runEffects(SpellTriggeredEvent<T> trigger, T event) {
        instance.getEffects(trigger).forEach(effect -> effect.run(this, trigger, event));
    }

    public Player player() {
        return player;
    }
    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(player.getUniqueId());
    }
    @Nullable
    public Player getOnlinePlayer() {
        return Bukkit.getPlayer(player.getUniqueId());
    }

    public SpellInstance instance() {
        return instance;
    }

    public Location location() {
        return location;
    }

    public @Nullable CastContext parent() {
        return parent;
    }

    public void finish() {
        if (hasFinished) {
            throw new IllegalStateException("Finish invoked twice on cast context.");
        }
        if (this.finishCallback != null) {
            this.hasFinished = true;
            this.finishCallback.run();
        }
        CastingManager.stopCasting(player);
    }

    public void fail() {
        if (hasFinished) {
            throw new IllegalStateException("Fail invoked twice on cast context.");
        }
        if (this.failCallback != null) {
            this.hasFinished = true;
            this.failCallback.run();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CastContext) obj;
        return Objects.equals(this.player, that.player) &&
                Objects.equals(this.instance, that.instance) &&
                Objects.equals(this.location, that.location) &&
                Objects.equals(this.parent, that.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, instance, location, parent);
    }
}
