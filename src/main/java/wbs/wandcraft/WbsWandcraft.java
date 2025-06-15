package wbs.wandcraft;

import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import wbs.utils.util.commands.brigadier.WbsCommand;
import wbs.utils.util.commands.brigadier.WbsErrorsSubcommand;
import wbs.utils.util.commands.brigadier.WbsReloadSubcommand;
import wbs.utils.util.commands.brigadier.WbsSubcommand;
import wbs.utils.util.plugin.WbsPlugin;
import wbs.wandcraft.commands.*;
import wbs.wandcraft.events.*;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.wand.Wand;

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
                .setPermission("wbswandcraft.command")
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
                                new CommandEffectsModify(this, "effect"),
                                WbsSubcommand.simpleSubcommand(this, "wand", context -> {
                                    CommandSender sender = context.getSource().getSender();
                                    if (!(sender instanceof Player player)) {
                                        sendMessage("This command is only usable by players.", sender);
                                        return;
                                    }
                                    ItemStack item = player.getInventory().getItemInMainHand();
                                    Wand wand = Wand.getIfValid(item);
                                    if (wand == null) {
                                        sendMessage("Hold a wand to modify it.", sender);
                                        return;
                                    }
                                    player.openInventory(wand.getInventory(item).getInventory());
                                    player.clearActiveItem();
                                })
                        ),
                        WbsReloadSubcommand.getStatic(this, settings),
                        WbsErrorsSubcommand.getStatic(this, settings),
                        new CommandGenerateWand(this, "generate"),
                        WbsSubcommand.simpleSubcommand(this, "tutorial", context -> {
                            CommandSender sender = context.getSource().getSender();
                            sendMessage("Not implemented.", sender);
                        })
                )
                .inferSubPermissions()
                .addAliases("wbswandcraft", "wandc", "wwc")
                .register();

        registerListener(new WandInventoryEvents());
        registerListener(new WandEvents());
        registerListener(new ArtificingBlockEvents());
        registerListener(new ArtificingItemEvents());
        registerListener(new StatusEffectEvents());

        // Run next tick, when the plugin is fully enabled
        runSync(() -> {
            WandcraftRegistries.SPELLS.stream().forEach(SpellDefinition::registerEvents);
            WandcraftRegistries.STATUS_EFFECTS.stream().forEach(StatusEffect::registerEvents);
        });
    }
}
