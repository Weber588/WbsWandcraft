package wbs.wandcraft.events;

import io.papermc.paper.event.player.PlayerLecternPageChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Lectern;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.LecternView;
import org.bukkit.util.Vector;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spellbook.Spellbook;

public class SpellbookEvents implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onConsumeSpellbook(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();

        Spellbook spellbook = Spellbook.fromItem(item);
        if (spellbook == null) {
            return;
        }

        event.setCancelled(true);

        Player player = event.getPlayer();

        // Crafting, not crafter or workbench -- represents the internal player inventory. Returned when nothing open.
        InventoryType inventoryType = player.getOpenInventory().getType();
        if (inventoryType != InventoryType.CRAFTING && inventoryType != InventoryType.CREATIVE) {
            return;
        }

        spellbook.tryCasting(player, item);
    }

    @EventHandler
    public void onSpellbookOpen(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }
        Player player = event.getPlayer();

        ItemStack item = event.getItem();

        Spellbook spellbook = Spellbook.fromItem(item);
        if (spellbook != null) {
            if (player.isSneaking()) {
                WbsWandcraft.getInstance().runLater(() -> {
                    ItemStack activeItem = player.getActiveItem();
                    if (!activeItem.isEmpty()) {
                        int activeItemUsedTime = player.getActiveItemUsedTime();
                        WbsWandcraft.getInstance().runTimer(runnable -> {
                            Player updatedPlayer = Bukkit.getPlayer(player.getUniqueId());
                            if (updatedPlayer != null && updatedPlayer.getActiveItem().equals(activeItem) &&  activeItemUsedTime <= updatedPlayer.getActiveItemUsedTime()) {
                                spawnParticleWord(updatedPlayer);
                            } else {
                                runnable.cancel();
                            }
                        }, 1, 5);
                    }}, 1);
                return;
            }

            EquipmentSlot hand = event.getHand();
            if (hand != null) {
                player.swingHand(hand);
            }

            event.setCancelled(true);
            event.setUseItemInHand(Event.Result.DENY);
            spellbook.openBook(player);
        }
    }

    private static void spawnParticleWord(Player updatedPlayer) {
        Location playerLoc = WbsEntityUtil.getMiddleLocation(updatedPlayer);

        Vector offset = WbsEntityUtil.getFacingVector(updatedPlayer);
        offset.setY(0);
        offset = WbsMath.scaleVector(offset, 0.8);
        offset.setY(Math.round(Math.random() * 5) / 5.0);
        offset.rotateAroundY(Math.toRadians(-80 + Math.random() * 160));
        int direction = Math.random() > 0.5 ? 1 : -1;
        for (int i = 0; i < 5 + Math.random() * 10 ; i++) {
            if (WbsMath.chance(10)) {
                // Occasionally show gaps like spaces in words
                continue;
            }
            updatedPlayer.getWorld().spawnParticle(
                    Particle.ENCHANT,
                    playerLoc.clone().add(offset),
                    0,
                    0,
                    0,
                    0,
                    0
            );
            offset.rotateAroundY(direction * Math.toRadians(7));
        }
    }

    @EventHandler
    public void onSpellbookOpen(PlayerTakeLecternBookEvent event) {
        //noinspection ConstantValue
        if (event.getLectern() == null) {
            // Marked as NotNull, but can be null when using MenuType.LECTERN
            event.setCancelled(true);
            return;
        }

        ItemStack book = event.getBook();

        if (book == null) {
            return;
        }

        if (Spellbook.isSpellbook(book)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSpellbookTurnPage(PlayerLecternPageChangeEvent event) {
        ItemStack book = event.getBook();

        Spellbook spellbook = Spellbook.fromItem(book);
        if (spellbook != null) {
            Player player = event.getPlayer();
            spellbook.currentPage(event.getNewPage());
            ItemStack heldItem = player.getInventory().getItemInMainHand();
            if (Spellbook.isSpellbook(heldItem)) {
                spellbook.toItem(heldItem);
            } else {
                ItemStack offHandItem = player.getInventory().getItemInOffHand();
                if (Spellbook.isSpellbook(offHandItem)) {
                    spellbook.toItem(offHandItem);
                }
            }
            if (player.getOpenInventory() instanceof LecternView view) {
                Lectern holder = view.getTopInventory().getHolder();
                if (holder != null) {
                    player.sendBlockChange(holder.getLocation(), Material.AIR.createBlockData());
                    holder.getLocation().getBlock().setType(Material.AIR);

                    WbsWandcraft.getInstance().runAtEndOfTick(() -> {
                        Player updatedPlayer = Bukkit.getPlayer(player.getUniqueId());
                        if (updatedPlayer != null && updatedPlayer.isOnline() && updatedPlayer.getOpenInventory() instanceof LecternView updatedView) {
                            Lectern lectern = updatedView.getTopInventory().getHolder();
                            if (lectern != null) {
                                lectern.getBlock().setType(Material.AIR);
                            }
                        }
                    });
                }
            }
        }
    }

    @EventHandler
    public void onRightClickSpell(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick()) {
            return;
        }
        Player player = event.getPlayer();

        ItemStack item = event.getItem();

        SpellInstance spell = SpellInstance.fromItem(item);
        if (spell != null) {
            Spellbook.teachSpell(player, spell.getDefinition());
        }
    }
}
