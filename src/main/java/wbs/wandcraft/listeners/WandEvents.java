package wbs.wandcraft.listeners;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import net.kyori.adventure.util.Ticks;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.crafting.ArtificingConfig;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.modifier.SpellModifier;
import wbs.wandcraft.wand.Wand;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
public class WandEvents implements Listener {
    private static final boolean DEBUG = false;
    private static void debug(String message) {
        if (DEBUG) {
            WbsWandcraft.getInstance().getLogger().info(message);
        }
    }

    // TODO: Make both these events return the relevant items to the player's inventory.
    @EventHandler(ignoreCancelled = true)
    public void onCraft(CraftItemEvent event) {
        for (ItemStack itemIngredient : event.getInventory().getMatrix()) {
            if (Wand.isWand(itemIngredient) || SpellInstance.isSpellInstance(itemIngredient) || SpellModifier.isSpellModifier(itemIngredient)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCraft(PrepareItemCraftEvent event) {
        for (ItemStack itemIngredient : event.getInventory().getMatrix()) {
            if (Wand.isWand(itemIngredient) || SpellInstance.isSpellInstance(itemIngredient) || SpellModifier.isSpellModifier(itemIngredient)) {
                event.getInventory().setResult(ItemStack.empty());
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onConsumeWand(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();

        Wand wand = Wand.fromItem(item);
        if (wand == null) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();

        // Crafting, not crafter or workbench -- represents the internal player inventory. Returned when nothing open.
        InventoryType inventoryType = player.getOpenInventory().getType();
        if (inventoryType != InventoryType.CRAFTING && inventoryType != InventoryType.CREATIVE) {
            return;
        }

        // TODO: Add field to Wand "being_edited" that allows stateful tracking of when casting is possible, to
        //  avoid lag issues allowing players to cast while in the wand's menu.
        wand.tryCasting(player, item, event);
    }

    // Drop is called before interact, so track it so we can distinguish punch from drop when arm swings
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        droppedItemsThisTick.add(player);
        WbsWandcraft.getInstance().runLater(() -> droppedItemsThisTick.remove(player), 1);
    }

    private final Set<Player> droppedItemsThisTick = new HashSet<>();

    // Can't ignore cancelled for interacting with air :(
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWandClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        ItemStack item = event.getItem();

        if (item == null || event.useItemInHand() == Event.Result.DENY) {
            return;
        }

        Player player = event.getPlayer();

        Block clickedBlock = event.getClickedBlock();
        if (!player.isSneaking() && clickedBlock != null && event.useInteractedBlock() != Event.Result.DENY) {
            //noinspection deprecation
            if (clickedBlock.getType().isInteractable()) {
                return;
            }
        }


        if (droppedItemsThisTick.contains(player)) {
            return;
        }

        // Crafting, not crafter or workbench -- represents the internal player inventory. Returned when nothing open.
        InventoryType inventoryType = player.getOpenInventory().getType();
        if (inventoryType != InventoryType.CRAFTING && inventoryType != InventoryType.CREATIVE) {
            return;
        }

        Wand wand = Wand.fromItem(item);
        if (wand == null) {
            return;
        }

        if (clickedBlock != null && ArtificingConfig.isInstance(clickedBlock)) {
            event.setCancelled(true);
        } else {
            // Don't try casting if it's a wand with a consumable component -- it needs to complete an animation first.
            Consumable consumable = item.getData(DataComponentTypes.CONSUMABLE);
            if (consumable == null || consumable.consumeSeconds() < 1 / (float) Ticks.TICKS_PER_SECOND) {
                wand.tryCasting(player, item, event);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWandClick(PlayerInteractEntityEvent event) {
        EquipmentSlot hand = event.getHand();
        if (hand != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItem(hand);

        // Crafting, not crafter or workbench -- represents the internal player inventory. Returned when nothing open.
        InventoryType inventoryType = player.getOpenInventory().getType();
        if (inventoryType != InventoryType.CRAFTING && inventoryType != InventoryType.CREATIVE) {
            return;
        }

        Wand wand = Wand.fromItem(item);
        if (wand == null) {
            return;
        }

        Entity clicked = event.getRightClicked();
        if (clicked instanceof Interaction interaction && ArtificingConfig.getTable(interaction) != null) {
            player.swingMainHand();
            wand.startEditing(player, item);
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onDropWand(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        // Crafting, not crafter or workbench -- represents the internal player inventory. Returned when nothing open.
        InventoryType inventoryType = player.getOpenInventory().getType();
        if (inventoryType != InventoryType.CRAFTING && inventoryType != InventoryType.CREATIVE) {
            return;
        }

        ItemStack item = event.getItemDrop().getItemStack();

        Wand wand = Wand.fromItem(item);
        if (wand == null) {
            return;
        }

        wand.handleDrop(event, player, item);
    }
}
