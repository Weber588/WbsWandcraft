package wbs.wandcraft;

import org.bukkit.NamespacedKey;
import wbs.utils.util.commands.brigadier.WbsCommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.commands.*;
import wbs.wandcraft.events.WandEvents;
import wbs.wandcraft.events.WandInventoryEvents;

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

        WbsCommand.getStatic(this, "wandcraft")
                .addSubcommands(
                        WbsCommand.getStatic(this, "build").addSubcommands(
                                new CommandBuildWand(this, "wand"),
                                new CommandBuildSpell(this, "spell"),
                                new CommandBuildModifier(this, "modifier")
                        ),
                        WbsCommand.getStatic(this, "modify").addSubcommands(
                                new CommandAttributesModify(this, "attribute"),
                                new CommandEffectsModify(this, "effect")
                        )
                )
                .addAliases("wbswandcraft", "wandc", "wwc")
                .register();

        registerListener(new WandInventoryEvents());
        registerListener(new WandEvents());
    }
}
