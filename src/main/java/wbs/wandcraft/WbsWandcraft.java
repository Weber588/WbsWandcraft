package wbs.wandcraft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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

        if (Bukkit.getPluginManager().getPlugin("ResourcePackManager") != null) {
            getComponentLogger().info(Component.text("ResourcePackManager detected! Injecting resource pack.").color(NamedTextColor.GREEN));
            getComponentLogger().info(Component.text("Note: This will load last unless you add \"WbsWandcraft\" to the priority list in ResourcePackManager/config.yml").color(NamedTextColor.GREEN));

            try {
                Files.copy(getDataPath().resolve(
                                "wbswandcraft_resource_pack.zip"),
                        Path.of("plugins/ResourcePackManager/mixer/wbswandcraft_resource_pack"),
                        StandardCopyOption.REPLACE_EXISTING
                );
            } catch (IOException e) {
                getLogger().severe("Failed to copy resource pack to ResourcePackManager/mixer!");
            }
            /*
            ResourcePackManagerAPI.registerResourcePack(
                    getName(),
                    "WbsWandcraft/wbswandcraft_resource_pack.zip",
                    false,
                    false,
                    true,
                    true,
                    "wbswandcraft:wandcraft reload"
            );
             */
        }

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
