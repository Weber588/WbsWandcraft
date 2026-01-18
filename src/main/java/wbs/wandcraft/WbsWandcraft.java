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
import wbs.wandcraft.equipment.MagicEquipmentType;
import wbs.wandcraft.learning.RegistrableLearningMethod;
import wbs.wandcraft.listeners.*;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.util.ItemUtils;
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
    public void onEnable() {
        instance = this;

        this.settings = new WandcraftSettings(this);
        this.settings.reload();

        WbsCommand.getStatic(this, "wandcraft")
                .setPermission("wbswandcraft.command")
                .addSubcommands(
                        WbsCommand.getStatic(this, "spell").addSubcommands(
                                new CommandSpellLearn(this, "learn"),
                                new CommandSpellForget(this, "forget"),
                                new CommandModifyAttributes(this, "attribute"),
                                new CommandSpellInfo(this, "info"),
                                new CommandSpellBuild(this, "build"),
                                new CommandSpellGenerate(this, "generate")
                        ).inferSubPermissions(),
                        WbsCommand.getStatic(this, "wand").addSubcommands(
                                new CommandWandBuild(this, "build"),
                                new CommandWandInfo(this, "info"),
                                new CommandModifyAttributes(this, "attribute"),
                                new CommandWandGenerate(this, "generate"),
                                WbsSubcommand.simpleSubcommand(this, "modify", context -> {
                                    CommandSender sender = context.getSource().getSender();
                                    if (!(sender instanceof Player player)) {
                                        sendMessage("This command is only usable by players.", sender);
                                        return;
                                    }
                                    ItemStack item = player.getInventory().getItemInMainHand();
                                    Wand wand = Wand.fromItem(item);
                                    if (wand == null) {
                                        sendMessage("Hold a wand to modify it.", sender);
                                        return;
                                    }
                                    wand.startEditing(player, item);
                                })
                        ).inferSubPermissions(),
                        WbsCommand.getStatic(this, "modifier").addSubcommands(
                                new CommandModifierBuild(this, "build"),
                                new CommandModifyEffects(this, "effect"),
                                new CommandModifyAttributes(this, "attribute"),
                                new CommandModifierGenerate(this, "generate")
                        ).inferSubPermissions(),
                        WbsCommand.getStatic(this, "item").addSubcommands(
                                new CommandWandBuild(this, "wand"),
                                new CommandSpellBuild(this, "spell"),
                                new CommandEquipmentBuild(this, "equipment"),
                                new CommandSpellbookBuild(this, "spellbook"),
                                WbsSubcommand.simpleSubcommand(this, "artificer", context -> {
                                    Player sender = (Player) context.getSource().getSender();
                                    sendMessage("Got 1 artificing table", sender);
                                    sender.getInventory().addItem(settings.getArtificingConfig().getItem());
                                }),
                                WbsSubcommand.simpleSubcommand(this, "blank_scroll", context -> {
                                    Player sender = (Player) context.getSource().getSender();
                                    sendMessage("Got 1 blank scroll", sender);
                                    sender.getInventory().addItem(ItemUtils.buildBlankScroll());
                                })
                        ).inferSubPermissions(),
                        new CommandSpellCast(this, "cast"),
                        WbsReloadSubcommand.getStatic(this, settings),
                        WbsErrorsSubcommand.getStatic(this, settings)
                )
                .inferSubPermissions()
                .addAliases("wbswandcraft", "wandc", "wwc")
                .register();

        registerListener(new WandInventoryEvents());
        registerListener(new WandEvents());
        registerListener(new SpellEvents());
        registerListener(new SpellbookEvents());
        registerListener(new ArtificingTableEvents());
        registerListener(new StatusEffectEvents());
        registerListener(new MagicBlockEvents());
        registerListener(new RecipeEvents());
        registerListener(new LearningEvents());

        // Run next tick, when the plugin is fully enabled
        runSync(() -> {
            WandcraftRegistries.SPELLS.stream().forEach(SpellDefinition::registerEvents);
            WandcraftRegistries.STATUS_EFFECTS.stream().forEach(StatusEffect::registerEvents);
            WandcraftRegistries.MAGIC_EQUIPMENT_TYPES.stream().forEach(MagicEquipmentType::registerEvents);
            settings.getGenerationMethods().forEach(RegistrableLearningMethod::registerEvents);
        });
    }
}
