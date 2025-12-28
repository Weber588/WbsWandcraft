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
import wbs.wandcraft.effects.StatusEffect;
import wbs.wandcraft.events.*;
import wbs.wandcraft.spell.definitions.SpellDefinition;
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
                        WbsCommand.getStatic(this, "spell").addSubcommands(
                                new CommandSpellLearn(this, "learn"),
                                new CommandModifyAttributes(this, "attribute"),
                                new CommandInfo(this, "info"),
                                new CommandSpellBuild(this, "build")
                        ),
                        WbsCommand.getStatic(this, "wand").addSubcommands(
                                new CommandWandBuild(this, "build"),
                                new CommandModifyAttributes(this, "attribute"),
                                new CommandGenerateWand(this, "generate"),
                                WbsSubcommand.simpleSubcommand(this, "modify", context -> {
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
                                    wand.startEditing(player, item);
                                })
                        ),
                        WbsCommand.getStatic(this, "modifier").addSubcommands(
                                new CommandModifierBuild(this, "build"),
                                new CommandModifyEffects(this, "effect"),
                                new CommandModifyAttributes(this, "attribute")
                        ),
                        new CommandBuildSpellbook(this, "spellbook"),
                        new CommandModifyPlayer(this, "player"),
                        WbsSubcommand.simpleSubcommand(this, "artificer", context -> {
                            Player sender = (Player) context.getSource().getSender();
                            sendMessage("Gave 1 artificing table!", sender);
                            sender.getInventory().addItem(settings.getArtificingConfig().getItem());
                        }),
                        WbsReloadSubcommand.getStatic(this, settings),
                        WbsErrorsSubcommand.getStatic(this, settings)
                )
                .inferSubPermissions()
                .addAliases("wbswandcraft", "wandc", "wwc")
                .register();

        registerListener(new WandInventoryEvents());
        registerListener(new WandEvents());
        registerListener(new SpellbookEvents());
        registerListener(new ArtificingBlockEvents());
        registerListener(new ArtificingItemEvents());
        registerListener(new StatusEffectEvents());
        registerListener(new MagicBlockEvents());

        // Run next tick, when the plugin is fully enabled
        runSync(() -> {
            WandcraftRegistries.SPELLS.stream().forEach(SpellDefinition::registerEvents);
            WandcraftRegistries.STATUS_EFFECTS.stream().forEach(StatusEffect::registerEvents);
        });
    }
}
