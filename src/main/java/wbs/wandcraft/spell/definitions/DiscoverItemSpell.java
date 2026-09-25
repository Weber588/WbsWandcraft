package wbs.wandcraft.spell.definitions;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.body.ItemDialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.keys.tags.ItemTypeTagKeys;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.util.Ticks;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.TileState;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.map.MinecraftFont;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import wbs.utils.util.WbsLocationUtil;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.context.CastContext;
import wbs.wandcraft.spell.attributes.attributable.RadiusAttributable;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;

import java.util.*;

import static wbs.wandcraft.spell.SpellType.ARCANE;

public class DiscoverItemSpell extends SpellDefinition implements CastableSpell, RadiusAttributable {
    public static final int BOLD_SPACE_WIDTH = 5;
    public static final int NORMAL_SPACE_WIDTH = 4;
    private static final Set<Material> WHITELISTED_TILE_ENTITY_DISPLAYS = Set.of(
            Material.FURNACE,
            Material.SMOKER,
            Material.BLAST_FURNACE,
            Material.JUKEBOX,
            Material.DECORATED_POT,
            Material.CAMPFIRE,
            Material.SOUL_CAMPFIRE,
            Material.BEACON,
            Material.SHULKER_BOX,
            Material.WHITE_SHULKER_BOX,
            Material.LIGHT_GRAY_SHULKER_BOX,
            Material.GRAY_SHULKER_BOX,
            Material.BLACK_SHULKER_BOX,
            Material.BROWN_SHULKER_BOX,
            Material.RED_SHULKER_BOX,
            Material.ORANGE_SHULKER_BOX,
            Material.YELLOW_SHULKER_BOX,
            Material.LIME_SHULKER_BOX,
            Material.GREEN_SHULKER_BOX,
            Material.CYAN_SHULKER_BOX,
            Material.LIGHT_BLUE_SHULKER_BOX,
            Material.BLUE_SHULKER_BOX,
            Material.PURPLE_SHULKER_BOX,
            Material.MAGENTA_SHULKER_BOX,
            Material.PINK_SHULKER_BOX
    );

    public DiscoverItemSpell() {
        super("discover_item");

        addSpellType(ARCANE);

        setAttribute(COST, 25);
        setAttribute(COOLDOWN, 2 * Ticks.TICKS_PER_SECOND);

        setAttribute(RADIUS, 20d);
    }

    @Override
    protected String rawDescription() {
        return "Searches nearby containers for copies of the item in your off hand, and highlights them.";
    }

    @Override
    public void cast(CastContext context) {
        Player player = context.player();

        SpellInstance instance = context.instance();
        double radius = instance.getAttribute(RADIUS);

        Map<Block, Container> nearbyContainers = getContainersWith(context, radius, null);

        WbsWandcraft plugin = WbsWandcraft.getInstance();

        if (nearbyContainers.isEmpty()) {
            plugin.sendActionBar("No nearby containers!", player);
            return;
        }

        Map<ItemStack, Integer> containedItems = new HashMap<>();

        nearbyContainers.forEach(((block, container) -> {
            for (ItemStack item : getInventory(container)) {
                if (item != null) {
                    boolean found = false;
                    for (ItemStack existing : containedItems.keySet()) {
                        if (item.isSimilar(existing)) {
                            found = true;

                            containedItems.put(existing, containedItems.get(existing) + item.getAmount());
                            break;
                        }
                    }

                    if (!found) {
                        containedItems.put(item.asOne(), item.getAmount());
                    }
                }
            }
        }));

        if (containedItems.isEmpty()) {
            plugin.sendActionBar("No items in nearby containers!", player);
            return;
        }

        Dialog dialog = getDialog(context, radius, containedItems, "");

        player.showDialog(dialog);
    }

    private Dialog getDialog(CastContext context, double radius, @UnknownNullability Map<ItemStack, Integer> containedItems, @NotNull String search) {
        List<DialogBody> displays = new LinkedList<>();

        Registry<ItemType> registry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM);
        TagKey<ItemType> tagSearch = null;
        String modifiedSearch;
        if (!search.startsWith("#")) {
            modifiedSearch = search;
        } else {
            modifiedSearch = search.substring(1);
            NamespacedKey namespacedKey = NamespacedKey.fromString(modifiedSearch);
            if (namespacedKey != null) {
                tagSearch = ItemTypeTagKeys.create(namespacedKey);

                if (!registry.hasTag(tagSearch)) {
                    tagSearch = null;
                }
            }
        }

        LinkedHashMap<ItemStack, Integer> orderedItems = new LinkedHashMap<>();
        TagKey<ItemType> finalTagSearch = tagSearch;
        containedItems.entrySet()
                .stream()
                .filter(entry -> {
                    ItemStack item = entry.getKey();

                    if (finalTagSearch != null) {
                        return registry.getTagValues(finalTagSearch).contains(item.getType().asItemType());
                    }

                    String qualifiedItemString = getQualifiedItemString(item);
                    //context.player().sendMessage(qualifiedItemString);
                    return Strings.isBlank(modifiedSearch) ||
                                    qualifiedItemString.matches(modifiedSearch.toLowerCase());
                        }
                ).sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEachOrdered(entry -> orderedItems.put(entry.getKey(), entry.getValue()));

        int maxWidth = 0;
        for (ItemStack item : orderedItems.keySet()) {
            String asString = PlainTextComponentSerializer.plainText().serialize(item.effectiveName());
            asString = asString + " x" + containedItems.get(item);

            maxWidth = Math.max(MinecraftFont.Font.getWidth(asString), maxWidth);
        }

        int dialogWidth = maxWidth + BOLD_SPACE_WIDTH * 2;

        for (ItemStack item : orderedItems.keySet()) {
            String asString = PlainTextComponentSerializer.plainText().serialize(item.effectiveName());
            int amount = containedItems.get(item);
            asString = asString + " x" + amount;

            int width = MinecraftFont.Font.getWidth(asString);
            int offset = maxWidth - width;

            int boldSpaces = offset % NORMAL_SPACE_WIDTH;
            int normalSpaces = (offset - BOLD_SPACE_WIDTH * boldSpaces) / NORMAL_SPACE_WIDTH;
            if (normalSpaces < 0) {
                boldSpaces = offset / BOLD_SPACE_WIDTH;
                normalSpaces = 0;
            }

            Component displayText = item.effectiveName()
                    .append(Component.text(" x" + amount))
                    .append(
                            Component.text(" ".repeat(boldSpaces)).decorate(TextDecoration.BOLD)
                    )
                    .append(
                            Component.text(" ".repeat(normalSpaces))
                    );
            ItemDialogBody dialogBody = DialogBody.item(item).description(
                    DialogBody.plainMessage(
                            displayText.clickEvent(
                                    ClickEvent.callback(audience -> {
                                        if (audience instanceof Player player) {
                                            highlightContainersWith(context, radius, item, player);
                                        }
                                    })
                            ).hoverEvent(Component.text("Click to search!")),
                            Math.clamp(dialogWidth, 0, 1024)
                    )
            ).build();

            displays.add(dialogBody);
        }

        List<ActionButton> buttons = List.of(
                ActionButton.builder(Component.text("Search"))
                        .action(DialogAction.customClick((view, audience) -> {
                            if (!(audience instanceof Player player)) {
                                return;
                            }
                            String newSearch = view.getText("search");

                            if (newSearch != null && !Objects.equals(search, newSearch)) {
                                Dialog dialog = getDialog(context, radius, containedItems, newSearch);
                                player.showDialog(dialog);
                            }
                        }, ClickCallback.Options.builder().uses(1).build()))
                        .build()
        );

        return Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(displayName())
                        .body(displays)
                        .inputs(
                                List.of(
                                        DialogInput.text("search", Component.text("Search"))
                                                .initial(search)
                                                .build()
                                )
                        )
                        .build()
                )
                .type(
                        DialogType.multiAction(buttons)
                                .columns(2)
                                .build()
                )
        );
    }

    private static @NonNull String getQualifiedItemString(ItemStack item) {
        return item.getType().getKey().asMinimalString() + item.getItemMeta().getAsComponentString();
    }

    private static void highlightContainersWith(CastContext context, double radius, ItemStack searchItem, Player player) {
        WbsWandcraft plugin = WbsWandcraft.getInstance();
        Map<Block, Container> allContainers = getContainersWith(context, radius, searchItem);

        Set<Block> containingBlocks = new HashSet<>();

        // Collect inventories into a set so we count distinctly for showing message later
        Set<Inventory> inventories = new HashSet<>();
        for (Block block : allContainers.keySet()) {
            // Recheck these, as the blocks or contents may have changed since the dialog was created.
            if (block.getState() instanceof Container container) {
                Inventory containerInventory = getInventory(container);
                if (containerInventory.containsAtLeast(searchItem, 1)) {
                    containingBlocks.add(block);
                    inventories.add(containerInventory);
                }
            }
        }

        Set<BlockDisplay> displays = new HashSet<>();
        containingBlocks.forEach(block -> {
            context.location().getWorld().spawn(block.getLocation(), BlockDisplay.class, display -> {
                // Most blocks can be set to themselves, but some (like chests) break when trying -- don't show tile
                // entities, unless we know they work and are in the whitelist
                if (block.getState() instanceof TileState && !WHITELISTED_TILE_ENTITY_DISPLAYS.contains(block.getType())) {
                    display.setBlock(Material.GRAY_STAINED_GLASS.createBlockData());
                } else {
                    display.setBlock(block.getBlockData());
                }

                Transformation transformation = display.getTransformation();
                float scale = 1.0001f;
                transformation.getScale().set(scale);
                transformation.getTranslation().set(-scale / 2 + 0.5);
                display.setTransformation(transformation);
                display.setBrightness(new Display.Brightness(15, 15));

                displays.add(display);

                display.setPersistent(false);
                display.setGlowing(true);
                display.setGlowColorOverride(Color.GREEN);

                display.setVisibleByDefault(false);
                player.showEntity(plugin, display);
            });
        });

        if (!displays.isEmpty()) {
            plugin.runLater(() -> displays.forEach(Entity::remove), 60L);
            plugin.buildMessageNoPrefix(inventories.size() + " inventories contain ")
                    .append(searchItem.effectiveName().applyFallbackStyle(plugin.getDefaultStyle()))
                    .build()
                    .sendActionBar(player);
        } else {
            plugin.buildMessageNoPrefix("No matches found.")
                    .build()
                    .sendActionBar(player);
        }
    }

    private static @NonNull Inventory getInventory(Container container) {
        Inventory containerInventory;
        if (container instanceof Chest chest) {
            containerInventory = chest.getBlockInventory();
        } else {
            containerInventory = container.getInventory();
        }
        return containerInventory;
    }

    private static @NonNull Map<Block, Container> getContainersWith(CastContext context, double radius, @Nullable ItemStack searchItem) {
        Map<Block, Container> allContainers = new HashMap<>();

        for (Block block : WbsLocationUtil.getNearbyBlocksSphere(context.location(), radius)) {
            if (block.getState() instanceof Container container) {
                Inventory inventory = getInventory(container);
                if (searchItem == null || inventory.containsAtLeast(searchItem, 1)) {
                    allContainers.put(block, container);
                }
            }
        }
        return allContainers;
    }
}
