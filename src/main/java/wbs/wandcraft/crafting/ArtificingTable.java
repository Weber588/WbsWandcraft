package wbs.wandcraft.crafting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.Ticks;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.particles.NormalParticleEffect;
import wbs.utils.util.persistent.BlockChunkStorageUtil;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.type.SpellType;
import wbs.wandcraft.spellbook.Spellbook;
import wbs.wandcraft.util.ItemUtils;
import wbs.wandcraft.wand.Wand;
import wbs.wandcraft.wand.types.WandType;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@NullMarked
public class ArtificingTable implements InventoryHolder {
    private static final NamespacedKey SPAWN_TIME = WbsWandcraft.getKey("spawn_time");
    private static final NamespacedKey ARTIFICING_INVENTORY = WbsWandcraft.getKey("artificing_inventory");
    private static final NormalParticleEffect CONVERT_EFFECT = (NormalParticleEffect) new NormalParticleEffect()
            .setXYZ(0.5)
            .setSpeed(0.1)
            .setAmount(10);

    private static final Team COLOUR_TEAM;

    static {
        String teamName = ArtificingConfig.TAG.asString().replaceAll(":", "_");
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager()
                .getMainScoreboard();
        Team team = mainScoreboard
                .getTeam(teamName);

        if (team == null) {
            team = mainScoreboard.registerNewTeam(teamName);
        }

        COLOUR_TEAM = team;

        // TODO: Make this colour configurable
        COLOUR_TEAM.color(NamedTextColor.AQUA);
    }

    private final Block block;
    private final @NotNull Inventory inventory;

    public ArtificingTable(Block block) {
        this.block = block;
        inventory = Bukkit.createInventory(this, InventoryType.HOPPER, Component.text("Echo Shard Storage"));

        PersistentDataContainer container = BlockChunkStorageUtil.getContainer(block);

        List<ItemStack> itemStacks = container.get(ARTIFICING_INVENTORY, PersistentDataType.LIST.listTypeFrom(WbsPersistentDataType.ITEM_AS_BYTES));

        if (itemStacks != null && !itemStacks.isEmpty()) {
            itemStacks.forEach(inventory::addItem);
        }
    }

    public void interact(Player player, EquipmentSlot hand) {
        Item item = getItem();

        if (player.isSneaking()) {
            dropItem();
        } else {
            ItemStack heldItem = player.getInventory().getItem(hand);
            Wand wand = Wand.fromItem(heldItem);
            if (wand != null) {
                wand.startEditing(player, heldItem);
                return;
            }
            Spellbook spellbook = Spellbook.fromItem(heldItem);
            if (spellbook != null) {
                handleSpellbookClick(player, spellbook);
                return;
            }

            if (item == null && (heldItem.isSimilar(ItemUtils.buildBlankScroll()) || heldItem.isSimilar(ItemStack.of(Material.STICK)))) {
                Item droppedItem = player.dropItem(hand, 1);

                if (droppedItem != null) {
                    acceptItem(droppedItem);
                } else {
                    throw new IllegalStateException("Failed to drop item previously checked?");
                }
            } else {
                player.openInventory(inventory);
            }
        }
    }

    private void dropItem() {
        Item item = getItem();
        if (item != null) {
            dropFromTable(item);
        }
    }

    private void handleSpellbookClick(Player player, Spellbook spellbook) {
        Item item = getItem();
        if (item == null) {
            // TODO: Find a better way to signal this
            player.sendActionBar(Component.text("No item on table!"));
            return;
        }

        SpellDefinition currentSpell = spellbook.getCurrentSpell();
        WandType<?> currentWandType = spellbook.getCurrentWandType();

        boolean hasWandIngredient = item.getItemStack().equals(ItemStack.of(Material.STICK));
        boolean hasSpellIngredient = item.getItemStack().equals(ItemUtils.buildBlankScroll());

        // TODO: Add a recipe system (ingredient, wand/spell def, echo shard cost)
        if (currentSpell != null) {
            if (hasSpellIngredient) {
                handleSpellCraft(player, item, currentSpell);
                return;
            }
        } else if (currentWandType != null) {
            if (hasWandIngredient) {
                handleWandCraft(player, item, currentWandType);
                return;
            }
        }

        if (hasWandIngredient) {
            WbsWandcraft.getInstance().sendActionBar("Select a wand page in the book", player);
        } else if (hasSpellIngredient) {
            WbsWandcraft.getInstance().sendActionBar("Select a spell page in the book", player);
        } else {
            player.sendActionBar(Component.text("No item on table!"));
        }
    }

    private void handleWandCraft(Player player, Item item, WandType<?> currentWandType) {
        int shardsAvailable = 0;
        for (ItemStack content : getInventory().getContents()) {
            if (content != null) {
                shardsAvailable += content.getAmount();
            }
        }

        int cost = currentWandType.getEchoShardCost();

        if (shardsAvailable >= cost) {
            getInventory().removeItem(ItemStack.of(Material.ECHO_SHARD, cost));
            save();

            item.setItemStack(ItemUtils.buildWand(currentWandType));
            dropItem();
            CONVERT_EFFECT.play(Particle.END_ROD, item.getLocation().add(0, 0.15, 0));
        } else {
            WbsWandcraft.getInstance().sendActionBar("Not enough echo shards! (" + shardsAvailable + "/" + cost + ")", player);
        }
    }

    private void handleSpellCraft(Player player, Item item, SpellDefinition currentSpell) {
        if (!Spellbook.getKnownSpells(player).contains(currentSpell)) {
            player.sendActionBar(Spellbook.getErrorMessage(currentSpell));
        } else {
            int shardsAvailable = 0;
            for (ItemStack content : getInventory().getContents()) {
                if (content != null) {
                    shardsAvailable += content.getAmount();
                }
            }

            int cost = currentSpell.getEchoShardCost();

            if (shardsAvailable >= cost) {
                getInventory().removeItem(ItemStack.of(Material.ECHO_SHARD, cost));
                save();

                item.setItemStack(ItemUtils.buildSpell(currentSpell));
                dropItem();
                List<SpellType> types = currentSpell.getTypes();
                Color color1 = types.get(0).color();
                Color color2 = color1;
                if (types.size() > 1) {
                    color2 = types.get(1).color();
                }
                CONVERT_EFFECT.setData(new Particle.DustTransition(color1, color2, 1.3f))
                        .play(Particle.DUST_COLOR_TRANSITION, item.getLocation().add(0, 0.15, 0));
            } else {
                WbsWandcraft.getInstance().sendActionBar("Not enough echo shards! (" + shardsAvailable + "/" + cost + ")", player);
            }
        }
    }

    public void save() {
        BlockChunkStorageUtil.modifyContainer(block, container -> {
            container.set(ARTIFICING_INVENTORY, PersistentDataType.LIST.listTypeFrom(WbsPersistentDataType.ITEM_AS_BYTES), Arrays.asList(inventory.getContents()));
        });
    }

    public Location getCentralItemLocation() {
        // TODO: Make the y offset configurable
        return block.getLocation().add(0.5, 1.2, 0.5);
    }

    @Nullable
    public Item getItem() {
        NamespacedKey blockKey = ArtificingConfig.getBlockKey(block);

        // Sort by age, oldest first -- that's the order they were added. First item is at the centre.
        return block.getWorld().getNearbyEntities(
                        BoundingBox.of(block).expand(32),
                        entity -> Objects.equals(
                                entity.getPersistentDataContainer().get(ArtificingConfig.TAG, WbsPersistentDataType.NAMESPACED_KEY),
                                blockKey
                        )
                ).stream()
                .map(entity -> {
                    if (entity instanceof Item item) {
                        return item;
                    }
                    return null;
                }).filter(Objects::nonNull)
                .min(Comparator.comparing(item -> item.getLocation().distance(block.getLocation().toCenterLocation())))
                .orElse(null);
    }

    public Item acceptItem(Item spawningItem) {
        if (getItem() != null) {
            throw new IllegalStateException("Table is not able to accept new items.");
        }

        Location centralItemLocation = getCentralItemLocation();

        spawningItem.setGravity(false);
        spawningItem.setGlowing(true);
        COLOUR_TEAM.addEntities(spawningItem);

        spawningItem.setVelocity(new Vector());

        spawningItem.setCanPlayerPickup(false);
        spawningItem.setCanMobPickup(false);
        spawningItem.setWillAge(false);

        PersistentDataContainer container = spawningItem.getPersistentDataContainer();
        container.set(SPAWN_TIME, PersistentDataType.LONG, System.currentTimeMillis());
        container.set(ArtificingConfig.TAG, WbsPersistentDataType.NAMESPACED_KEY, ArtificingConfig.getBlockKey(block));

        spawningItem.teleport(centralItemLocation);
        return spawningItem;
    }

    public void breakTable() {
        ArtificingConfig.unregisterTable(this);

        NamespacedKey blockKey = ArtificingConfig.getBlockKey(block);

        Item item = getItem();
        if (item != null) {
            dropFromTable(item);
        }

        inventory.forEach(inventoryStack -> {
            if (inventoryStack != null) block.getWorld().dropItemNaturally(block.getLocation().toCenterLocation(), inventoryStack);
        });

        BlockChunkStorageUtil.modifyContainer(block, container -> {
            container.remove(ARTIFICING_INVENTORY);
        });

        block.getWorld().dropItemNaturally(
                block.getLocation().toCenterLocation(),
                WbsWandcraft.getInstance().getSettings().getArtificingConfig().getItem()
        );

        block.getWorld().getNearbyEntities(
                BoundingBox.of(block).expand(32),
                entity -> entity.getPersistentDataContainer().has(blockKey)
        ).forEach(Entity::remove);

        block.setBlockData(Material.AIR.createBlockData());
    }

    private static void dropFromTable(Item item) {
        ItemStack stack = item.getItemStack();
        item.getWorld().dropItem(item.getLocation(), stack, spawned -> {
            spawned.setVelocity(new Vector(0, 0.2, 0));
            spawned.setPickupDelay(2 * Ticks.TICKS_PER_SECOND);
        });
        item.remove();
    }

    public Block getBlock() {
        return block;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

}
