package wbs.wandcraft.generator;

import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.WbsCollectionUtil;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.util.ItemUtils;
import wbs.wandcraft.wand.Wand;
import wbs.wandcraft.wand.WandHolder;
import wbs.wandcraft.wand.WandInventoryType;
import wbs.wandcraft.wand.WandTexture;

import java.util.*;

public class WandGenerator implements Keyed {
    private final List<WandInventoryType> types = new LinkedList<>(WandcraftRegistries.WAND_INVENTORY_TYPES.values());

    private final List<WandTexture> textures = new LinkedList<>(WandcraftRegistries.WAND_TEXTURES.values());
    private final double hue;

    private final int minAttributes;
    private final int maxAttributes;
    private final List<AttributeInstanceGenerator<?>> attributeGenerators = new LinkedList<>();

    private final int minSpells;
    private final int maxSpells;

    private final List<SpellInstanceGenerator> spellGenerators = new LinkedList<>();
    private final @NotNull NamespacedKey key;

    public WandGenerator(@NotNull NamespacedKey key, int minAttributes, int maxAttributes, double hue, int minSpells, int maxSpells) {
        this.key = key;
        this.hue = hue;
        this.minAttributes = minAttributes;
        this.maxAttributes = maxAttributes;
        this.minSpells = minSpells;
        this.maxSpells = maxSpells;
    }

    public ItemStack get() {
        if (types.isEmpty()) {
            types.addAll(WandcraftRegistries.WAND_INVENTORY_TYPES.values());
        }
        WandInventoryType inventoryType = WbsCollectionUtil.getRandom(types);

        WandTexture texture = WbsCollectionUtil.getRandom(textures);

        ItemStack wandItem = ItemUtils.buildWand(inventoryType, texture, hue, ItemUseAnimation.BLOCK, 1f / 4);

        if (!attributeGenerators.isEmpty()) {
            int attributes = new Random().nextInt(minAttributes, maxAttributes + 1);
            for (int i = 0; i < attributes; i++) {
                AttributeInstanceGenerator<?> attributeGenerator = WbsCollectionUtil.getRandom(attributeGenerators);

                ItemUtils.modifyItem(wandItem, attributeGenerator.get(), AttributeModifierType.SET);
            }
        }

        if (!spellGenerators.isEmpty()) {
            WandHolder wandHolder = Objects.requireNonNull(Wand.getIfValid(wandItem))
                    .getInventory(wandItem);
            Inventory inventory = wandHolder.getInventory();

            int spells = new Random().nextInt(minSpells, maxSpells + 1);
            for (int i = 0; i < spells; i++) {
                SpellInstanceGenerator spellGenerator = WbsCollectionUtil.getRandom(spellGenerators);
                SpellInstance instance = spellGenerator.get();

                ItemStack instanceItem = ItemUtils.buildSpell(instance);

                inventory.addItem(instanceItem);
            }

            List<@Nullable ItemStack> contentsList = Arrays.asList(inventory.getContents());
            Collections.shuffle(contentsList);
            inventory.setContents(contentsList.toArray(ItemStack[]::new));

            wandHolder.save();
        }

        return wandItem;
    }

    @Override
    public @NotNull NamespacedKey getKey() {
        return key;
    }

    public WandGenerator setTextures(WandTexture ... textures) {
        return setTextures(Arrays.asList(textures));
    }
    public WandGenerator setTextures(List<WandTexture> textures) {
        this.textures.clear();

        if (textures.isEmpty()) {
            this.textures.addAll(WandcraftRegistries.WAND_TEXTURES.values());
        } else {
            this.textures.addAll(textures);
        }

        return this;
    }

    public WandGenerator setTypes(WandInventoryType ... types) {
        return setTypes(Arrays.asList(types));
    }
    public WandGenerator setTypes(List<WandInventoryType> types) {
        this.types.clear();

        if (types.isEmpty()) {
            this.types.addAll(WandcraftRegistries.WAND_INVENTORY_TYPES.values());
        } else {
            this.types.addAll(types);
        }

        return this;
    }

    public WandGenerator addSpellGenerator(SpellInstanceGenerator generator) {
        spellGenerators.add(generator);
        return this;
    }

    public WandGenerator addAttributeGenerator(AttributeInstanceGenerator<?> generator) {
        attributeGenerators.add(generator);
        return this;
    }
}
