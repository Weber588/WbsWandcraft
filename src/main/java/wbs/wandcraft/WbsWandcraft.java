package wbs.wandcraft;

import org.bukkit.NamespacedKey;
import wbs.utils.util.plugin.WbsPlugin;

public class WbsWandcraft extends WbsPlugin {

    public static NamespacedKey getKey(String key) {
        return new NamespacedKey(getInstance(), key);
    }

    private static WbsWandcraft instance;
    public static WbsWandcraft getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
    }
}
