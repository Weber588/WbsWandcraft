package wbs.wandcraft.spell.definitions;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.TileState;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Transformation;
import wbs.utils.util.WbsLocationUtil;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.definitions.extensions.CastContext;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.RadiusedSpell;

import java.util.HashSet;
import java.util.Set;

public class DiscoverItemSpell extends SpellDefinition implements CastableSpell, RadiusedSpell {
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
    }

    @Override
    public Component description() {
        return Component.text("Searches nearby containers for copies of the item in your off hand, and highlights them.");
    }

    @Override
    public void cast(CastContext context) {
        Player player = context.player();
        PlayerInventory inventory = player.getInventory();

        ItemStack offHandItem = inventory.getItemInOffHand();
        WbsWandcraft plugin = WbsWandcraft.getInstance();
        if (offHandItem.isEmpty()) {
            plugin.buildMessageNoPrefix("Hold an item in your off hand!")
                    .build()
                    .sendActionBar(player);
            return;
        }

        SpellInstance instance = context.instance();
        double radius = instance.getAttribute(RADIUS);

        Set<Block> containingBlocks = new HashSet<>();

        // Collect inventories into a set so we count distinctly for showing message later
        Set<Inventory> inventories = new HashSet<>();
        for (Block block : WbsLocationUtil.getNearbyBlocksSphere(player.getEyeLocation(), radius)) {
            if (block.getState() instanceof Container container) {
                Inventory containerInventory = container.getInventory();
                if (containerInventory.containsAtLeast(offHandItem, 1)) {
                    containingBlocks.add(block);
                    inventories.add(containerInventory);
                }
            }
        }

        Set<BlockDisplay> displays = new HashSet<>();
        containingBlocks.forEach(block -> {
            player.getWorld().spawn(block.getLocation(), BlockDisplay.class, display -> {
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
            plugin.buildMessageNoPrefix(inventories.size() + " containers found!")
                    .build()
                    .sendActionBar(player);
        } else {
            plugin.buildMessageNoPrefix("No matches found.")
                    .build()
                    .sendActionBar(player);
        }
    }
}
