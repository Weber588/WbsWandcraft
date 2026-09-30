package wbs.wandcraft.listeners;

import io.papermc.paper.event.player.PlayerLecternPageChangeEvent;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
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
import wbs.utils.util.pluginhooks.hooks.PacketEventsWrapper;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.crafting.ArtificingConfig;
import wbs.wandcraft.crafting.ArtificingTable;
import wbs.wandcraft.spell.SpellType;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spellbook.Spellbook;
import wbs.wandcraft.util.EffectUtils;
import wbs.wandcraft.util.ItemUtils;
import wbs.wandcraft.util.MenuUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class SpellbookEvents implements Listener {
    public static final int INTERPOLATION_DURATION = (int) (2.5 * Ticks.TICKS_PER_SECOND);
    public static final Vector3f UP_VECTOR = new Vector3f(0, 1, 0);
    public static final Vector DEFAULT_DIRECTION = BlockFace.SOUTH.getDirection();
    public static final Vector NORTH = BlockFace.NORTH.getDirection();

    private static final int WORD_ROWS = 7;
    private static final double OFFSET_INTERVAL = 0.2;
    private static final List<Double> WORD_ROW_OFFSETS;

    static {
        WORD_ROW_OFFSETS = new LinkedList<>();
        for (int i = 0; i < WORD_ROWS; i++) {
            WORD_ROW_OFFSETS.add(OFFSET_INTERVAL * (i - 1));
        }
    }

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

        Block clickedBlock = event.getClickedBlock();
        if (!player.isSneaking() && clickedBlock != null && event.useInteractedBlock() != Event.Result.DENY) {
            //noinspection deprecation
            if (clickedBlock.getType().isInteractable()) {
                return;
            }
        }

        Spellbook spellbook = Spellbook.fromItem(item);
        if (spellbook != null) {
            SpellDefinition currentSpell = spellbook.getCurrentSpell();
            if (currentSpell != null && player.isSneaking()) {
                boolean knowsSpell = Spellbook.knowsSpell(player, currentSpell);

                List<Double> history = new LinkedList<>();

                WbsWandcraft.getInstance().runLater(() -> {
                    ItemStack activeItem = player.getActiveItem();
                    if (!activeItem.isEmpty()) {
                        int activeItemUsedTime = player.getActiveItemUsedTime();
                        WbsWandcraft.getInstance().runTimer(runnable -> {
                            Player updatedPlayer = Bukkit.getPlayer(player.getUniqueId());
                            if (updatedPlayer == null) {
                                runnable.cancel();
                                return;
                            }

                            if (knowsSpell) {
                                int remainingTicks = updatedPlayer.getActiveItemRemainingTime();
                                int remainingSeconds = (int) Math.ceil(((double) remainingTicks) / Ticks.TICKS_PER_SECOND);
                                if (remainingSeconds > 0) {
                                    Component remainingTimeMessage = Component.empty().append(Component.text(remainingSeconds)).append(Component.text("..."))
                                            .color(MenuUtils.DESCRIPTION_COLOR)
                                            .decorate(TextDecoration.ITALIC);

                                    updatedPlayer.sendActionBar(remainingTimeMessage);
                                }
                            }

                            if (!updatedPlayer.getActiveItem().equals(activeItem) || activeItemUsedTime > updatedPlayer.getActiveItemUsedTime()) {
                                runnable.cancel();
                                return;
                            }

                            spawnParticleWord(updatedPlayer, currentSpell,
                                    WbsCollectionUtil.getAvoidRepeats(
                                            () -> WbsCollectionUtil.getRandom(WORD_ROW_OFFSETS),
                                            WORD_ROW_OFFSETS.size(),
                                            history,
                                            1.5
                                    )
                            );
                        }, 1, 5);
                    }}, 1);
                return;
            }

            event.setUseItemInHand(Event.Result.DENY);

            if (clickedBlock != null) {
                ArtificingTable table = ArtificingConfig.getTable(clickedBlock);
                if (table != null) {
                    if (currentSpell != null) {
                        return;
                    }
                }
            }
            spellbook.openBook(player);
        }
    }

    private static final String CHARS_IN_ILLAGERALT = "abcdefghijklmnopqrstuvwxyz";

    private static void spawnParticleWord(Player updatedPlayer, SpellDefinition spell, double yOffset) {
        Location playerLoc = WbsEntityUtil.getMiddleLocation(updatedPlayer);

        TextColor color = getWordColour(spell);

        boolean knowsSpell = Spellbook.getKnownSpells(updatedPlayer).contains(spell);

        float scaleValue = 0.7f;
        Vector3f scale = new Vector3f(scaleValue, scaleValue, scaleValue);
        Vector3f minScale = new Vector3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

        Vector offset = WbsEntityUtil.getFacingVector(updatedPlayer);
        offset.setY(0);
        offset = WbsMath.scaleVector(offset, 1.2 * scaleValue);

        Random random = new Random();
        World world = updatedPlayer.getWorld();

        double initialAngle = Math.toRadians(-45 + Math.random() * 90);
        offset.rotateAroundY(initialAngle);
        int direction = Math.random() > 0.5 ? 1 : -1;

        float angleBetweenGlyphs = (float) (direction * Math.toRadians(7));
        double animationRotation = Math.toRadians(45) * direction;

        // TODO: Optimize this to only create 1 runnable
        int characters = (int) (5 + Math.random() * 5);
        for (int charIndex = 0; charIndex < characters; charIndex++) {
            if (WbsMath.chance(10)) {
                // Occasionally show gaps like spaces in words
                continue;
            }

            Location spawnLoc = playerLoc.clone();
            spawnLoc.setDirection(DEFAULT_DIRECTION);
            spawnLoc.add(0, yOffset, 0);

            Component glyph = Component.text(CHARS_IN_ILLAGERALT.charAt(random.nextInt(CHARS_IN_ILLAGERALT.length())))
                    .color(color)
                    .font(Key.key("illageralt"));

            if (!knowsSpell && WbsMath.chance(30)) {
                glyph = glyph.decorate(TextDecoration.OBFUSCATED);
            }

            Vector3f initialTranslation = offset.toVector3f();
            float angleFromNorth = NORTH.angle(Vector.fromJOML(initialTranslation));
            if (initialTranslation.x >= 0) {
                angleFromNorth *= -1;
            }
            AxisAngle4f startingRotation = new AxisAngle4f(
                    angleFromNorth,
                    UP_VECTOR
            );

            TextDisplay entity = EffectUtils.showTextDisplay(
                    spawnLoc,
                    glyph,
                    new Transformation(
                            initialTranslation,
                            startingRotation,
                            scale,
                            new AxisAngle4f()
                    ),
                    display -> {

                    }
            );

            AxisAngle4f startingRotationReversed = new AxisAngle4f(
                    (float) Math.abs((angleFromNorth + Math.PI) % Math.TAU),
                    UP_VECTOR
            );
            TextDisplay reversed = EffectUtils.showTextDisplay(
                    spawnLoc,
                    glyph,
                    new Transformation(
                            initialTranslation,
                            startingRotationReversed,
                            scale,
                            new AxisAngle4f()
                    ),
                    display -> {

                    }
            );

            Vector3f endingTranslation = offset.clone().rotateAroundY(animationRotation).toVector3f();
            float checkAngle = NORTH.angle(Vector.fromJOML(endingTranslation));
            if (endingTranslation.x >= 0) {
                checkAngle *= -1;
            }
            final float finalRotationAngle = checkAngle;
            AxisAngle4f finalRotation = new AxisAngle4f(
                    finalRotationAngle,
                    UP_VECTOR
            );
            float finalReversedRotation = (float) Math.abs((finalRotationAngle + Math.PI) % Math.TAU);
            AxisAngle4f finalRotationReversed = new AxisAngle4f(
                    finalReversedRotation,
                    UP_VECTOR
            );

            WbsWandcraft.getInstance().runLater(() -> {
                entity.setTransformation(new Transformation(
                        endingTranslation,
                        finalRotation,
                        scale,
                        new AxisAngle4f()
                ));
                reversed.setTransformation(new Transformation(
                        endingTranslation,
                        finalRotationReversed,
                        scale,
                        new AxisAngle4f()
                ));

                entity.setTextOpacity(Byte.MIN_VALUE);
                reversed.setTextOpacity(Byte.MIN_VALUE);

                PacketEventsWrapper.get().ifPresent(pe -> {
                    for (Player player : world.getPlayersSeeingChunk(updatedPlayer.getChunk())) {
                        pe.updateEntity(entity, player);
                        pe.updateEntity(reversed, player);
                    }
                });
            }, 1);

            long removeAnimationDelay = INTERPOLATION_DURATION - ((charIndex + characters) * 2L) - updatedPlayer.getPing() / Ticks.SINGLE_TICK_DURATION_MS;
            int removeAnimationDuration = 10;
            WbsWandcraft.getInstance().runLater(() -> {
                entity.setInterpolationDelay(0);
                entity.setInterpolationDuration(removeAnimationDuration);
                reversed.setInterpolationDelay(0);
                reversed.setInterpolationDuration(removeAnimationDuration);

                Vector3f randomizedTranslation = endingTranslation.add(
                        (float) (Math.random() * OFFSET_INTERVAL),
                        (float) (Math.random() * OFFSET_INTERVAL),
                        (float) (Math.random() * OFFSET_INTERVAL)
                );
                entity.setTransformation(new Transformation(
                        randomizedTranslation,
                        finalRotation,
                        minScale,
                        new AxisAngle4f()
                ));

                reversed.setTransformation(new Transformation(
                        randomizedTranslation,
                        finalRotationReversed,
                        minScale,
                        new AxisAngle4f()
                ));

                PacketEventsWrapper.get().ifPresent(pe -> {
                    for (Player player : world.getPlayersSeeingChunk(updatedPlayer.getChunk())) {
                        pe.updateEntity(entity, player);
                        pe.updateEntity(reversed, player);
                    }
                });
            }, removeAnimationDelay);

            WbsWandcraft.getInstance().runLater(() -> {
                for (Player player : world.getPlayersSeeingChunk(updatedPlayer.getChunk())) {
                    removeEntity(player, entity);
                    removeEntity(player, reversed);
                }
            }, removeAnimationDelay + removeAnimationDuration);

            offset.rotateAroundY(angleBetweenGlyphs);
        }
    }

    private static void removeEntity(Player player, TextDisplay entity) {
        PacketEventsWrapper.get().ifPresentOrElse(
                pe -> pe.removeEntity(entity, player),
                entity::remove
        );
    }

    private static void showFakeEntity(Player player, TextDisplay entity) {
        PacketEventsWrapper.get().ifPresent(pe -> pe.showFakeEntity(entity, player));
    }

    private static @NotNull TextColor getWordColour(SpellDefinition spell) {
        TextColor color;
        if (spell == null) {
            color = MenuUtils.EXTRAS_COLOUR;
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

    private static @NotNull String toComponents(Vector3f translation) {
        Vector vector = new Vector(0, 0, 1);
        float angle = (float) Math.toDegrees(vector.angle(Vector.fromJOML(translation)));
        return translation.x + ", " + translation.y + ", " + translation.z + ": (" + angle + ")";
    }

    @EventHandler
    public void onSpellbookTake(PlayerTakeLecternBookEvent event) {
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

        if (ItemUtils.isWandcraftItem(book)) {
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
            if (ItemUtils.isWandcraftItem(book)) {
                Player player = (Player) event.getPlayer();
                PacketEventsWrapper.get().ifPresent(pe -> pe.sendGameModeChange(player.getGameMode(), player));
            }
        }
    }
}
