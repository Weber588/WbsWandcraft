package wbs.wandcraft.listeners;

import io.papermc.paper.event.player.PlayerLecternPageChangeEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Lectern;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerTakeLecternBookEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.view.LecternView;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;
import wbs.utils.util.WbsCollectionUtil;
import wbs.utils.util.WbsMath;
import wbs.utils.util.entities.WbsEntityUtil;
import wbs.utils.util.pluginhooks.PacketEventsWrapper;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.crafting.ArtificingConfig;
import wbs.wandcraft.crafting.ArtificingTable;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.type.SpellType;
import wbs.wandcraft.spellbook.Spellbook;
import wbs.wandcraft.util.EffectUtils;

import java.util.Random;

public class SpellbookEvents implements Listener {
    public static final int INTERPOLATION_DURATION = (int) (2.5 * Ticks.TICKS_PER_SECOND);

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
                                SpellDefinition currentSpell = spellbook.getCurrentSpell();

                                spawnParticleWord(updatedPlayer, currentSpell);
                            } else {
                                runnable.cancel();
                            }
                        }, 1, 5);
                    }}, 1);
                return;
            }

            EquipmentSlot hand = event.getHand();
            if (hand == null) {
                hand = EquipmentSlot.HAND;
            }

            player.swingHand(hand);

            event.setUseItemInHand(Event.Result.DENY);

            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null) {
                ArtificingTable table = ArtificingConfig.getTable(clickedBlock);
                if (table != null) {
                    SpellDefinition currentSpell = spellbook.getCurrentSpell();

                    if (currentSpell != null) {
                        return;
                    }
                }
            }
            spellbook.openBook(player);
        }
    }

    private static final String CHARS_IN_ILLAGERALT = "abcdefghijklmnopqrstuvwxyz";

    private static void spawnParticleWord(Player updatedPlayer, SpellDefinition spell) {
        Location playerLoc = WbsEntityUtil.getMiddleLocation(updatedPlayer);

        TextColor color = getWordColour(spell);

        boolean knowsSpell = Spellbook.getKnownSpells(updatedPlayer).contains(spell);

        float scaleValue = 0.7f;
        Vector3f scale = new Vector3f(scaleValue, scaleValue, scaleValue);

        Vector offset = WbsEntityUtil.getFacingVector(updatedPlayer);
        offset.setY(0);
        offset = WbsMath.scaleVector(offset, 1.2 * scaleValue);
        double yOffset = Math.round(Math.random() * 5) / 5.0;

        Random random = new Random();
        World world = updatedPlayer.getWorld();

        double initialAngle = Math.toRadians(-45 + Math.random() * 90);
        offset.rotateAroundY(initialAngle);
        int direction = Math.random() > 0.5 ? 1 : -1;

        float angleBetweenGlyphs = (float) (direction * Math.toRadians(7));
        double animationRotation = Math.toRadians(45) * direction;

        Vector entityFacing = BlockFace.SOUTH.getDirection();
        Vector north = BlockFace.NORTH.getDirection();
        Vector3f yVector = new Vector3f(0, 1, 0);

        // TODO: Optimize this to only create 1 runnable
        int characters = (int) (5 + Math.random() * 5);
        for (int charIndex = 0; charIndex < characters; charIndex++) {
            if (WbsMath.chance(10)) {
                // Occasionally show gaps like spaces in words
                continue;
            }

            Location spawnLoc = playerLoc.clone();
            spawnLoc.setDirection(entityFacing);
            spawnLoc.add(0, yOffset, 0);

            Component glyph = Component.text(CHARS_IN_ILLAGERALT.charAt(random.nextInt(CHARS_IN_ILLAGERALT.length())))
                    .color(color)
                    .font(Key.key("illageralt"));

            if (!knowsSpell && WbsMath.chance(30)) {
                glyph = glyph.decorate(TextDecoration.OBFUSCATED);
            }

            Vector3f translation = offset.toVector3f();
            float angleFromNorth = north.angle(Vector.fromJOML(translation));
            if (translation.x >= 0) {
                angleFromNorth *= -1;
            }
            AxisAngle4f startingRotation = new AxisAngle4f(
                    angleFromNorth,
                    yVector
            );

            TextDisplay entity = getTextDisplay(spawnLoc, glyph, translation, startingRotation, scale);

            AxisAngle4f startingRotationReversed = new AxisAngle4f(
                    (float) Math.abs((angleFromNorth + Math.PI) % Math.TAU),
                    yVector
            );
            TextDisplay reversed = getTextDisplay(spawnLoc, glyph, translation, startingRotationReversed, scale);

            for (Player player : world.getPlayersSeeingChunk(updatedPlayer.getChunk())) {
                PacketEventsWrapper.showFakeEntity(player, entity);
                PacketEventsWrapper.showFakeEntity(player, reversed);
            }


            Vector3f finalTranslation = offset.clone().rotateAroundY(animationRotation).toVector3f();
            float finalAngleFromNorth = north.angle(Vector.fromJOML(finalTranslation));
            if (finalTranslation.x >= 0) {
                finalAngleFromNorth *= -1;
            }
            AxisAngle4f finalRotation = new AxisAngle4f(
                    finalAngleFromNorth,
                    yVector
            );
            AxisAngle4f finalRotationReversed = new AxisAngle4f(
                    (float) Math.abs((finalAngleFromNorth + Math.PI) % Math.TAU),
                    yVector
            );

            WbsWandcraft.getInstance().runLater(() -> {
                entity.setTransformation(new Transformation(
                        finalTranslation,
                        finalRotation,
                        scale,
                        new AxisAngle4f()
                ));
                reversed.setTransformation(new Transformation(
                        finalTranslation,
                        finalRotationReversed,
                        scale,
                        new AxisAngle4f()
                ));

                entity.setTextOpacity(Byte.MIN_VALUE);
                reversed.setTextOpacity(Byte.MIN_VALUE);

                for (Player player : world.getPlayersSeeingChunk(updatedPlayer.getChunk())) {
                    PacketEventsWrapper.updateEntity(player, entity);
                    PacketEventsWrapper.updateEntity(player, reversed);
                }
            }, 1);

            WbsWandcraft.getInstance().runLater(() -> {
                for (Player player : world.getPlayersSeeingChunk(updatedPlayer.getChunk())) {
                    PacketEventsWrapper.removeEntity(player, entity);
                    PacketEventsWrapper.removeEntity(player, reversed);
                }

            }, INTERPOLATION_DURATION - charIndex + characters - updatedPlayer.getPing() / Ticks.SINGLE_TICK_DURATION_MS);

            offset.rotateAroundY(angleBetweenGlyphs);
        }
    }

    private static @NotNull TextColor getWordColour(SpellDefinition spell) {
        TextColor color;
        if (spell == null) {
            color = NamedTextColor.GOLD;
        } else {
            if (WbsMath.chance(50)) {
                SpellType type = WbsCollectionUtil.getRandom(spell.getTypes());
                color = type.textColor();
            } else {
                color = spell.getPrimarySpellType().textColor();
            }
        }
        return color;
    }

    private static @NotNull TextDisplay getTextDisplay(Location spawnLoc, Component glyph, Vector3f translation, AxisAngle4f startingRotation, Vector3f scale) {
        TextDisplay entity = EffectUtils.getGlyphDisplay(glyph, spawnLoc, translation, scale, startingRotation, new AxisAngle4f());

        entity.setInterpolationDuration(INTERPOLATION_DURATION);
        entity.setTeleportDuration(INTERPOLATION_DURATION);

        return entity;
    }

    private static @NotNull String toComponents(Vector3f translation) {
        Vector vector = new Vector(0, 0, 1);
        float angle = (float) Math.toDegrees(vector.angle(Vector.fromJOML(translation)));
        return translation.x + ", " + translation.y + ", " + translation.z + ": (" + angle + ")";
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
    public void onDamage(EntityDamageEvent event) {
        if (!(event instanceof Player player)) {
            return;
        }

        ItemStack activeItem = player.getActiveItem();
        if (Spellbook.isSpellbook(activeItem)) {

            double chancePerHealth = 0.5;

            double chance = 1 - (Math.pow(1 - chancePerHealth, event.getDamage()));

            if (Math.random() < chance) {
                WbsWandcraft.getInstance().sendActionBar("Interrupted!", player);
                player.clearActiveItem();
            }
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
    public void onCloseLectern(InventoryCloseEvent event) {
        if (event.getView() instanceof LecternView view) {
            ItemStack book = view.getTopInventory().getItem(0);
            if (Spellbook.isSpellbook(book)) {
                Player player = (Player) event.getPlayer();
                PacketEventsWrapper.sendGameModeChange(player, player.getGameMode());
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
