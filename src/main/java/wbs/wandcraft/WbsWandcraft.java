package wbs.wandcraft;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import wbs.utils.util.commands.brigadier.WbsCommand;
import wbs.utils.util.commands.brigadier.WbsErrorsSubcommand;
import wbs.utils.util.commands.brigadier.WbsReloadSubcommand;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.commands.*;
import wbs.wandcraft.events.ArtificingBlockEvents;
import wbs.wandcraft.events.ArtificingItemEvents;
import wbs.wandcraft.events.WandEvents;
import wbs.wandcraft.events.WandInventoryEvents;

@SuppressWarnings("UnstableApiUsage")
public class WbsWandcraft extends WbsPlugin {
    public static NamespacedKey getKey(String key) {
        return new NamespacedKey(getInstance(), key);
    }

    private static WbsWandcraft instance;
    public static WbsWandcraft getInstance() {
        return instance;
    }

    private WandcraftSettings settings;

    @Override
    public WandcraftSettings getSettings() {
        return settings;
    }

    @Override
    public void onLoad() {
        instance = this;

        this.settings = new WandcraftSettings(this);
        this.settings.reload();
    }

    @Override
    public void onEnable() {
        WbsCommand.getStatic(this, "wandcraft")
                .addSubcommands(
                        new CommandInfo(this, "info"),
                        WbsCommand.getStatic(this, "build").addSubcommands(
                                new CommandBuildWand(this, "wand"),
                                new CommandBuildSpell(this, "spell"),
                                new CommandBuildModifier(this, "modifier"),
                                WbsSubcommand.simpleSubcommand(this, "artificer", context -> {
                                    Player sender = (Player) context.getSource().getSender();
                                    sendMessage("Gave 1 artificing table!", sender);
                                    sender.getInventory().addItem(settings.getArtificingConfig().getItem());
                                })
                        ),
                        WbsCommand.getStatic(this, "modify").addSubcommands(
                                new CommandAttributesModify(this, "attribute"),
                                new CommandEffectsModify(this, "effect")
                        ),
                        WbsReloadSubcommand.getStatic(this, settings),
                        WbsErrorsSubcommand.getStatic(this, settings),
                        WbsSubcommand.simpleSubcommand(this, "tutorial", context -> {
                            CommandSender sender = context.getSource().getSender();
                            sendMessage("Not implemented.", sender);
                        })
                )
                .addAliases("wbswandcraft", "wandc", "wwc")
                .register();

        registerListener(new WandInventoryEvents());
        registerListener(new WandEvents());
        registerListener(new ArtificingBlockEvents());
        registerListener(new ArtificingItemEvents());
    }
}
