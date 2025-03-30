package wbs.wandcraft;

import wbs.utils.util.plugin.WbsSettings;

public class WandcraftSettings extends WbsSettings {
    protected WandcraftSettings(WbsWandcraft plugin) {
        super(plugin);
    }

    @Override
    public void reload() {
        loadDefaultConfig("config.yml");
    }
}
