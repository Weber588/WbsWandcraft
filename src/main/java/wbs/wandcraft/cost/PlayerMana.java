package wbs.wandcraft.cost;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataHolder;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import wbs.wandcraft.WbsWandcraft;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerMana {
    private static final Map<UUID, ManaContext> CURRENT_MANA_BARS = new HashMap<>();

    public static final int DEFAULT_MAX_MANA = 1000;
    public static final int DEFAULT_MANA_REGENERATION = 20; // In mana/tick
    public static final int DEFAULT_MANA_REGEN_COOLOFF = 20; // In ticks

    public static final NamespacedKey MAX_MANA_KEY = WbsWandcraft.getKey("max_mana");
    public static final NamespacedKey MANA_KEY = WbsWandcraft.getKey("mana");
    public static final NamespacedKey MANA_REGENERATION_KEY = WbsWandcraft.getKey("mana_regeneration");
    public static final NamespacedKey LAST_USED_MANA_KEY = WbsWandcraft.getKey("last_used_mana");

    private int mana;
    private int maxMana;
    private int manaRegenerationRate;
    private long lastUsedMana;

    public PlayerMana(int mana, int maxMana, int manaRegenerationRate, long lastUsedMana) {
        this.mana = mana;
        this.maxMana = maxMana;
        this.manaRegenerationRate = manaRegenerationRate;
        this.lastUsedMana = lastUsedMana;
    }

    public PlayerMana(Player player) {
        this.mana = getOrDefault(player, MANA_KEY, PersistentDataType.INTEGER, DEFAULT_MAX_MANA);
        this.maxMana = getOrDefault(player, MAX_MANA_KEY, PersistentDataType.INTEGER, DEFAULT_MAX_MANA);
        this.manaRegenerationRate = getOrDefault(player, MANA_REGENERATION_KEY, PersistentDataType.INTEGER, DEFAULT_MANA_REGENERATION);
        this.lastUsedMana = getOrDefault(player, LAST_USED_MANA_KEY, PersistentDataType.LONG, 0L);
    }

    public void saveTo(Player player) {
        PersistentDataContainer container = player.getPersistentDataContainer();
        container.set(MANA_KEY, PersistentDataType.INTEGER, mana);
        container.set(MAX_MANA_KEY, PersistentDataType.INTEGER, maxMana);
        container.set(MANA_REGENERATION_KEY, PersistentDataType.INTEGER, manaRegenerationRate);
        container.set(LAST_USED_MANA_KEY, PersistentDataType.LONG, lastUsedMana);
    }

    public <T> T getOrDefault(PersistentDataHolder holder, NamespacedKey key, PersistentDataType<?, T> type, T defaultValue) {
        T value = holder.getPersistentDataContainer().get(key, type);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    public int getMana() {
        return mana;
    }

    public PlayerMana setMana(int mana) {
        this.mana = mana;
        return this;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public PlayerMana setMaxMana(int maxMana) {
        this.maxMana = maxMana;
        if (this.mana > maxMana) {
            this.mana = maxMana;
        }
        return this;
    }

    public int getManaRegenerationRate() {
        return manaRegenerationRate;
    }

    public PlayerMana setManaRegenerationRate(int manaRegenerationRate) {
        this.manaRegenerationRate = manaRegenerationRate;
        return this;
    }

    private void tickRegeneration() {
        this.mana = Math.min(this.maxMana, this.mana + this.manaRegenerationRate);
    }

    private long getUsableSystemMillis() {
        // TODO: Make mana regen cooloff an attribute on the player?
        return lastUsedMana + (DEFAULT_MANA_REGEN_COOLOFF * 1000 / Ticks.TICKS_PER_SECOND);
    }

    public int applyCost(Player player, int cost) {
        int remainder = 0;
        if (mana - cost >= 0) {
            mana -= cost;
        } else {
            remainder = cost - mana;
            mana = 0;
        }

        lastUsedMana = System.currentTimeMillis();

        showManaBar(player);

        saveTo(player);

        return remainder;
    }

    public void showManaBar(Player player) {
        ManaContext manaBar = CURRENT_MANA_BARS.get(player.getUniqueId());

        if (manaBar == null) {
            manaBar = new ManaContext(player, this);
        }

        manaBar.resetTimeLeftOnScreen();
    }

    private static class ManaContext {
        public static final int DEFAULT_TIME_ON_SCREEN = 3 * Ticks.TICKS_PER_SECOND;
        private final BossBar manaBar;
        private int timeLeftOnScreen = DEFAULT_TIME_ON_SCREEN;

        private ManaContext(Player player, PlayerMana currentMana) {
            CURRENT_MANA_BARS.put(player.getUniqueId(), this);

            TextComponent barName = Component.text("Mana").color(NamedTextColor.AQUA);
            manaBar = BossBar.bossBar(
                    barName,
                    currentMana.mana / (float) currentMana.maxMana,
                    BossBar.Color.BLUE,
                    BossBar.Overlay.PROGRESS
            );

            manaBar.addViewer(player);

            new BukkitRunnable() {
                @Override
                public void run() {
                    Player updatedPlayer = Bukkit.getPlayer(player.getUniqueId());
                    if (updatedPlayer == null || !updatedPlayer.isOnline()) {
                        CURRENT_MANA_BARS.remove(player.getUniqueId());
                        cancel();
                        return;
                    }

                    PlayerMana updatedMana = new PlayerMana(player);

                    if (updatedMana.getUsableSystemMillis() <= System.currentTimeMillis()) {
                        if (updatedMana.mana >= updatedMana.maxMana) {
                            timeLeftOnScreen--;
                        } else {
                            updatedMana.tickRegeneration();
                            updatedMana.saveTo(player);
                        }
                    }

                    manaBar.progress(updatedMana.mana / (float) updatedMana.maxMana);

                    if (timeLeftOnScreen <= 0) {
                        manaBar.removeViewer(player);
                        CURRENT_MANA_BARS.remove(player.getUniqueId());
                        cancel();
                    }
                }
            }.runTaskTimer(WbsWandcraft.getInstance(), 1L, 1L);
        }

        public void resetTimeLeftOnScreen() {
            timeLeftOnScreen = DEFAULT_TIME_ON_SCREEN;
        }
    }
}



