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
import org.bukkit.inventory.*;
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
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.type.SpellType;
import wbs.wandcraft.spellbook.Spellbook;
import wbs.wandcraft.util.ItemUtils;
import wbs.wandcraft.wand.Wand;
import wbs.wandcraft.wand.types.WandType;

import java.util.*;

@NullMarked
public class ArtificingTable implements InventoryHolder {
    public static final String DEBUG_CHANNEL_RECIPES = "recipes";

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
        inventory = Bukkit.createInventory(this, InventoryType.HOPPER, Component.text("Artificing Table Storage"));

        PersistentDataContainer container = BlockChunkStorageUtil.getContainer(block);

        List<ItemStack> itemStacks = container.get(ARTIFICING_INVENTORY, PersistentDataType.LIST.listTypeFrom(WbsPersistentDataType.ITEM_AS_BYTES));

        if (itemStacks != null && !itemStacks.isEmpty()) {
            itemStacks.forEach(inventory::addItem);
        }
    }

    public void interact(Player player, EquipmentSlot hand) {
        Item baseItem = getItem();

        ItemStack heldItem = player.getInventory().getItem(hand);
        Wand wand = Wand.fromItem(heldItem);
        boolean sneaking = player.isSneaking();
        if (wand != null && !sneaking) {
            wand.startEditing(player, heldItem);
            return;
        }

        if (tryCraftItem(player, hand, baseItem, heldItem)) {
            return;
        }

        if (sneaking) {
            if (wand != null) {
                wand.startEditing(player, heldItem);
            } else {
                List<Item> items = getItems();
                if (!items.isEmpty()) {
                    Item last = items.getLast();
                    dropFromTable(last);
                }
                dropShards();
            }
            return;
        }

        Spellbook spellbook = Spellbook.fromItem(heldItem);
        if (spellbook != null) {
            handleSpellbookClick(player, spellbook);
            return;
        }

        player.openInventory(inventory);
    }

    private boolean tryCraftItem(Player player, EquipmentSlot hand, @Nullable Item baseItem, ItemStack heldItem) {
        WbsWandcraft plugin = WbsWandcraft.getInstance();
        List<ArtificingRecipe> recipes = plugin.getSettings().getArtificingRecipes();
        // Prevent non-ingredient recipes from taking priority when other ingredients are present
        recipes.sort(Comparator.comparing(recipe -> recipe.ingredient() == null));
        if (baseItem == null) {
            for (ArtificingRecipe recipe : recipes) {
                if (recipe.baseItem().test(heldItem)) {
                    Item droppedItem = player.dropItem(hand, 1);

                    if (droppedItem != null) {
                        acceptBaseItem(droppedItem);
                    } else {
                        throw new IllegalStateException("Failed to drop item previously checked?");
                    }

                    return true;
                }
            }
            return false;
        }

        List<Item> items = getItems();

        for (ArtificingRecipe recipe : recipes) {
            if (recipe.baseItem().test(baseItem.getItemStack())) {
                plugin.debug(DEBUG_CHANNEL_RECIPES, "Possible match for recipe " + recipe.key().asString());
                RecipeChoice ingredient = recipe.ingredient();
                int existingAmount = 0;
                int amount;
                if (heldItem.getType() == Material.ECHO_SHARD) {
                    if (ingredient != null) {
                        int existingIngredient = 0;
                        for (Item invItem : items) {
                            ItemStack stack = invItem.getItemStack();
                            if (ingredient.test(stack)) {
                                existingIngredient += stack.getAmount();
                            }
                        }

                        // Make the player finish the rest of the recipe before accepting shards, or it'll use a pseudo-random
                        // amount of shards
                        if (existingIngredient > 0 && existingIngredient < recipe.ingredientAmount()) {
                            // TODO: Replace this with spitting all other items off the table and playing a failure
                            //  effect or sound
                            player.sendActionBar(
                                    Component.text("Recipe incomplete... (%d/%d)".formatted(existingIngredient, recipe.ingredientAmount()))
                                            .style(plugin.getErrorStyle())
                            );
                            return true;
                        } else if (existingIngredient == 0) {
                            continue;
                        }
                    }

                    for (Item invItem : items) {
                        ItemStack stack = invItem.getItemStack();
                        if (stack.getType() == Material.ECHO_SHARD) {
                            existingAmount += stack.getAmount();
                        }
                    }
                    amount = recipe.shardCost();
                } else if (ingredient != null && ingredient.test(heldItem)) {
                    for (Item invItem : items) {
                        ItemStack stack = invItem.getItemStack();
                        if (ingredient.test(stack)) {
                            existingAmount += stack.getAmount();
                        }
                    }
                    amount = recipe.ingredientAmount();
                } else {
                    continue;
                }

                int toDrop = Math.min(heldItem.getAmount(), amount - existingAmount);

                if (toDrop >= 1) {
                    plugin.debug(
                            DEBUG_CHANNEL_RECIPES,
                            "Matched recipe %s\nDropping %s%s x%d"
                                    .formatted(
                                            recipe.key().asString(),
                                            heldItem.getType(),
                                            heldItem.getItemMeta().getAsComponentString(),
                                            toDrop
                                    )
                    );

                    Item droppedItem = player.dropItem(hand, toDrop);

                    if (droppedItem != null) {
                        acceptRecipeItem(recipe, droppedItem);
                    } else {
                        throw new IllegalStateException("Failed to drop item previously checked?");
                    }
                    return true;
                }
            }
        }

        return false;
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
            playGenericConvert(item.getLocation());
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
                playConvertSpell(item.getLocation(), currentSpell);
            } else {
                WbsWandcraft.getInstance().sendActionBar("Not enough echo shards! (" + shardsAvailable + "/" + cost + ")", player);
            }
        }
    }

    private static void playConvertSpell(Location location, SpellDefinition currentSpell) {
        List<SpellType> types = currentSpell.getTypes();
        Color color1 = types.get(0).color();
        Color color2 = color1;
        if (types.size() > 1) {
            color2 = types.get(1).color();
        }
        CONVERT_EFFECT.setData(new Particle.DustTransition(color1, color2, 1.3f))
                .play(Particle.DUST_COLOR_TRANSITION, location.add(0, 0.15, 0));
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
        return getItems().stream()
                .min(Comparator.comparing(item -> item.getLocation().distance(block.getLocation().toCenterLocation())))
                .orElse(null);
    }

    private List<Item> getItems() {
        NamespacedKey blockKey = ArtificingConfig.getBlockKey(block);

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
                .sorted(Comparator.comparingInt(Entity::getTicksLived).reversed())
                .toList();
    }

    public void acceptBaseItem(Item spawningItem) {
        if (getItem() != null) {
            throw new IllegalStateException("Table is not able to accept new items.");
        }

        acceptItem(spawningItem);
    }

    public void acceptRecipeItem(ArtificingRecipe recipe, Item spawningItem) {
        List<Item> items = new LinkedList<>(getItems());
        items.add(spawningItem);

        RecipeChoice ingredient = recipe.ingredient();

        Map<ItemStack, Integer> similarItemToAmount = new HashMap<>();
        for (Item item : items) {
            countItem(item, similarItemToAmount);
        }

        int echoShards = similarItemToAmount.getOrDefault(ItemStack.of(Material.ECHO_SHARD), 0);
        int existingAmount = 0;

        if (ingredient != null) {
            existingAmount = similarItemToAmount.keySet()
                    .stream()
                    .filter(ingredient)
                    .findFirst()
                    .map(match -> similarItemToAmount.getOrDefault(match, 0))
                    .orElse(0)
            ;
        }

        if (recipe.ingredientAmount() <= existingAmount && recipe.shardCost() <= echoShards) {
            craftItem(recipe, items, ingredient);
        }

        acceptItem(spawningItem);
    }

    private static void countItem(Item item, Map<ItemStack, Integer> similarItemToAmount) {
        ItemStack stack = item.getItemStack();
        Set<ItemStack> keys = similarItemToAmount.keySet();

        if (keys.isEmpty()) {
            similarItemToAmount.put(stack.asOne(), stack.getAmount());
        } else {
            keys.stream()
                    .filter(stack::isSimilar)
                    .findFirst()
                    .ifPresentOrElse(match -> {
                        int amount = similarItemToAmount.getOrDefault(match, 0);

                        amount += stack.getAmount();

                        similarItemToAmount.put(match, amount);
                    }, () -> {
                        similarItemToAmount.put(stack.asOne(), stack.getAmount());
                    });
        }
    }

    private void craftItem(ArtificingRecipe recipe, List<Item> items, @Nullable RecipeChoice ingredient) {
        ItemStack result = recipe.result();

        List<Item> toRemove = new LinkedList<>();
        for (Item item : items) {
            ItemStack stack = item.getItemStack();
            if ((recipe.baseItem().test(stack)) || (ingredient != null && ingredient.test(stack)) || stack.getType() == Material.ECHO_SHARD) {
                toRemove.add(item);
            }
        }

        for (Item item : toRemove) {
            item.remove();
        }

        Location location = getCentralItemLocation();

        SpellInstance spellInstance = SpellInstance.fromItem(result);
        if (spellInstance != null) {
            playConvertSpell(location, spellInstance.getDefinition());
        } else {
            playGenericConvert(location);
        }

        dropAtTable(result);
    }

    private static void playGenericConvert(Location location) {
        CONVERT_EFFECT.setData(null).play(Particle.END_ROD, location.add(0, 0.15, 0));
    }

    private void acceptItem(Item spawningItem) {
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
    }

    public void breakTable() {
        ArtificingConfig.unregisterTable(this);

        NamespacedKey blockKey = ArtificingConfig.getBlockKey(block);

        Item item = getItem();
        if (item != null) {
            dropFromTable(item);
        }

        dropShards();

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

    private void dropShards() {
        inventory.forEach(inventoryStack -> {
            if (inventoryStack != null) block.getWorld().dropItemNaturally(block.getLocation().toCenterLocation(), inventoryStack);
        });

        BlockChunkStorageUtil.modifyContainer(block, container -> {
            container.remove(ARTIFICING_INVENTORY);
        });
    }

    private void dropFromTable(Item item) {
        ItemStack stack = item.getItemStack();
        dropAtTable(stack);
        item.remove();
    }

    private void dropAtTable(ItemStack stack) {
        Location location = getCentralItemLocation();
        location.getWorld().dropItem(location, stack, spawned -> {
            spawned.setVelocity(new Vector(0, 0.2, 0));
            spawned.setPickupDelay(2 * Ticks.TICKS_PER_SECOND);
        });
    }

    public Block getBlock() {
        return block;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

}
