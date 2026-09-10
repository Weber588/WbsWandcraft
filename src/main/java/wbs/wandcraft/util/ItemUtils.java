package wbs.wandcraft.util;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.*;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.tag.Tag;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.Ticks;
import net.kyori.adventure.util.TriState;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.WbsWandcraftBootstrap;
import wbs.wandcraft.equipment.MagicEquipmentType;
import wbs.wandcraft.equipment.hat.MagicHat;
import wbs.wandcraft.resourcepack.ResourcePackBuilder;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.spell.definitions.SpellDefinition;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.modifier.SpellModifier;
import wbs.wandcraft.spellbook.Spellbook;
import wbs.wandcraft.wand.Wand;
import wbs.wandcraft.wand.types.WandType;

import java.util.List;
import java.util.UUID;

@NullMarked
@SuppressWarnings("UnstableApiUsage")
public class ItemUtils {
    public static final NamespacedKey WANDCRAFT_ITEM_NAME = WbsWandcraft.getKey("wandcraft_item_name");
    public static final NamespacedKey WANDCRAFT_ITEM_KEY = WbsWandcraft.getKey("wandcraft_item");
    public static final NamespacedKey KEY_BLANK_SCROLL = WbsWandcraft.getKey("blank_scroll");

    public static final Material BASE_MATERIAL_WAND = Material.STICK;
    public static final Material BASE_MATERIAL_SPELL = Material.FLOW_BANNER_PATTERN;
    public static final Material BASE_MATERIAL_MODIFIER = Material.GLOBE_BANNER_PATTERN;
    public static final Material BASE_MATERIAL_SPELLBOOK = Material.BOOK;
    public static final Material DISPLAY_MATERIAL_SPELLBOOK = Material.KNOWLEDGE_BOOK;
    public static final Material BASE_MATERIAL_HAT = Material.LEATHER_HELMET;
    public static final Material DISPLAY_MATERIAL_HAT = Material.BLACK_WOOL;
    public static final Material BASE_MATERIAL_BLANK_SCROLL = Material.PAPER;

    public static ItemStack buildBlankScroll() {
        ItemStack blankScroll = ItemStack.of(BASE_MATERIAL_BLANK_SCROLL);
        blankScroll.getDataTypes().forEach(blankScroll::unsetData);
        blankScroll.setData(DataComponentTypes.ITEM_NAME, Component.text("Blank Scroll"));

        blankScroll.setData(
                DataComponentTypes.CUSTOM_MODEL_DATA,
                CustomModelData.customModelData()
                        .addString(WbsWandcraft.getKey("blank_scroll").asString())
                        .build()
        );
        blankScroll.setData(DataComponentTypes.ITEM_MODEL, BASE_MATERIAL_BLANK_SCROLL.getKey());
        addHiddenEnchant(blankScroll);
        blankScroll.setData(DataComponentTypes.MAX_STACK_SIZE, 64);

        blankScroll.editPersistentDataContainer(container -> {
                    container.set(WANDCRAFT_ITEM_NAME, PersistentDataType.STRING, "Blank Scroll");
                    container.set(WANDCRAFT_ITEM_KEY, WbsPersistentDataType.NAMESPACED_KEY, WbsWandcraft.getKey("blank_scroll"));
                }
        );

        return blankScroll;
    }

    public static ItemStack buildHat(MagicHat hatType) {
        ItemStack hatItem = ItemStack.of(BASE_MATERIAL_HAT);
        hatItem.getDataTypes().forEach(hatItem::unsetData);

        hatType.getModel().applyCustomModelData(hatItem);

        hatItem.setData(DataComponentTypes.ITEM_MODEL, DISPLAY_MATERIAL_HAT.getKey());
        hatItem.setData(DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.HEAD)
                .damageOnHurt(false)
                .build()
        );
        hatItem.setData(DataComponentTypes.ENCHANTABLE, Enchantable.enchantable(25));
        hatItem.setData(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments().build());

        hatType.toItem(hatItem);

        return hatItem;
    }

    public static ItemStack buildWand(WandType<?> type) {
        ItemStack item = ItemStack.of(BASE_MATERIAL_WAND);

        Wand wand = type.newWand();

        item.getDataTypes().forEach(item::unsetData);
        item.setData(DataComponentTypes.ITEM_NAME, Component.text("Wand"));

        Tag<BlockType> tag = RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK).getTag(WbsWandcraftBootstrap.ALL_BLOCKS);

        // Allow spells to break block with correct tool
        item.setData(DataComponentTypes.TOOL,
                Tool.tool()
                        .addRule(Tool.rule(tag, Float.MIN_VALUE, TriState.TRUE))
                        .build()
        );
        item.setData(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(0.0001f)
                .cooldownGroup(WbsWandcraft.getKey(UUID.randomUUID().toString()))
        );

        type.getWandTexture().applyCustomModelData(item, builder -> builder.addColor(Color.WHITE));

        item.setData(DataComponentTypes.ITEM_MODEL, BASE_MATERIAL_WAND.getKey());
        addHiddenEnchant(item);

        ItemUseAnimation animation = type.getAnimation();
        if (animation != null && type.getAnimationTicks() >= 1) {
            item.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable()
                    .animation(animation)
                    .hasConsumeParticles(false)
                    .consumeSeconds((float) type.getAnimationTicks() / Ticks.TICKS_PER_SECOND)
                    .sound(Key.key("entity.illusioner.cast_spell"))
            );
        }
        
        NamespacedKey itemModel = WbsWandcraft.getInstance().getSettings().getItemModel("wand");
        
        if (itemModel == null) {
            itemModel = item.getType().getKey();
        }

        NamespacedKey finalItemModel = itemModel;
        item.editMeta(meta -> meta.setItemModel(finalItemModel));

        wand.toItem(item);

        return item;
    }

    public static ItemStack buildSpellbook() {
        ItemStack item = ItemStack.of(BASE_MATERIAL_SPELLBOOK);

        Spellbook spellbook = new Spellbook();

        item.getDataTypes().forEach(item::unsetData);
        item.setData(DataComponentTypes.ITEM_NAME, Component.text("Spellbook"));
        item.setData(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(0.0001f)
                .cooldownGroup(WbsWandcraft.getKey(UUID.randomUUID().toString()))
        );

//        if (hue == null || hue < 0) {
//            hue = Math.random();
//        }

        ResourcePackBuilder.ITEM_PROVIDER_SPELLBOOK.applyCustomModelData(item);

        item.setData(DataComponentTypes.ITEM_MODEL, DISPLAY_MATERIAL_SPELLBOOK.getKey());

        NamespacedKey itemModel = WbsWandcraft.getInstance().getSettings().getItemModel("spellbook");

        if (itemModel == null) {
            itemModel = DISPLAY_MATERIAL_SPELLBOOK.getKey();
        }

        NamespacedKey finalItemModel = itemModel;
        item.editMeta(meta -> meta.setItemModel(finalItemModel));
        addHiddenEnchant(item);

        spellbook.toItem(item);

        return item;
    }

    private static void addHiddenEnchant(ItemStack item) {
        item.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false);
        item.setData(DataComponentTypes.ENCHANTMENTS,
                ItemEnchantments.itemEnchantments()
                        .add(Enchantment.BINDING_CURSE, 1)
                        .build()
        );
        item.setData(DataComponentTypes.TOOLTIP_DISPLAY,
                TooltipDisplay.tooltipDisplay()
                        .addHiddenComponents(DataComponentTypes.ENCHANTMENTS)
                        .build()
        );
    }

    public static ItemStack buildSpell(SpellDefinition spell) {
        ItemStack item = ItemStack.of(BASE_MATERIAL_SPELL);
        SpellInstance spellInstance = new SpellInstance(spell);

        item.getDataTypes().forEach(item::unsetData);

        spellInstance.getDefinition().applyCustomModelData(item, builder -> builder.addColor(spell.getPrimarySpellType().color()));

        NamespacedKey itemModelKey = BASE_MATERIAL_SPELL.getKey();

        item.setData(DataComponentTypes.ITEM_MODEL, itemModelKey);
        addHiddenEnchant(item);

        item.setData(DataComponentTypes.USE_COOLDOWN, UseCooldown.useCooldown(0.0001f)
                .cooldownGroup(WbsWandcraft.getKey(UUID.randomUUID().toString()))
        );

        spellInstance.toItem(item);
        item.editPersistentDataContainer(container -> {
                    container.set(WANDCRAFT_ITEM_NAME, PersistentDataType.STRING, spellInstance.getDefinition().name());
                    container.set(WANDCRAFT_ITEM_KEY, WbsPersistentDataType.NAMESPACED_KEY, spellInstance.getDefinition().getKey());
                }
        );
        return item;
    }

    public static ItemStack buildSpell(SpellInstance instance) {
        ItemStack item = buildSpell(instance.getDefinition());

        instance.toItem(item);

        return item;
    }

    public static ItemStack buildModifier() {
        return buildModifier(null);
    }
    public static ItemStack buildModifier(@Nullable SpellAttributeModifier<?, ?> attributeModifier) {
        ItemStack item = ItemStack.of(BASE_MATERIAL_MODIFIER);
        SpellModifier modifier = new SpellModifier();
        if (attributeModifier != null) {
            modifier.addModifier(attributeModifier);
        }

        item.getDataTypes().forEach(item::unsetData);

        NamespacedKey itemModelKey = BASE_MATERIAL_MODIFIER.getKey();
        
        item.setData(DataComponentTypes.ITEM_MODEL, itemModelKey);
        addHiddenEnchant(item);

        modifier.toItem(item);
        item.editPersistentDataContainer(container -> {
                    container.set(WANDCRAFT_ITEM_NAME, PersistentDataType.STRING, "Spell Modifier");
                    container.set(WANDCRAFT_ITEM_KEY, WbsPersistentDataType.NAMESPACED_KEY, modifier.getTypeKey());
                }
        );
        return item;
    }
    
    public static AttributeModificationResult modifyItem(ItemStack item, SpellAttributeInstance<?> attributeInstance, AttributeModifierType modifierType) {
        SpellModifier spellModifier = SpellModifier.fromItem(item);
        Wand wand = Wand.fromItem(item);
        SpellInstance instance = SpellInstance.fromItem(item);

        if (spellModifier != null) {
            modifyModifier(item, attributeInstance, modifierType, spellModifier);
            return AttributeModificationResult.MODIFIED_MODIFIER;
        } else if (wand != null) {
            // Only set real attribute if the wand already contains it -- wands only do things with specific attributes, and all are always present.
            return modifyWand(item, attributeInstance, modifierType, attributeInstance.attribute(), wand);
        } else if (instance != null) {
            modifySpellInstance(item, attributeInstance, instance);
            return AttributeModificationResult.MODIFIED_SPELL;
        } else {
            return AttributeModificationResult.INVALID_ITEM;
        }
    }

    public static void modifySpellInstance(ItemStack item, SpellAttributeInstance<?> attributeInstance, SpellInstance instance) {
        instance.setAttribute(attributeInstance);

        instance.toItem(item);
    }

    public static AttributeModificationResult modifyWand(ItemStack item, SpellAttributeInstance<?> attributeInstance, AttributeModifierType modifierType, SpellAttribute<?> attribute, Wand wand) {
        if (wand.getAttributeInstances().stream().anyMatch(value -> value.attribute().equals(attribute))) {
            wand.setAttribute(attributeInstance);
            wand.toItem(item);
            return AttributeModificationResult.MODIFIED_WAND_ATTRIBUTE;
        } else {
            wand.setModifier(attributeInstance.createModifier(modifierType));
            wand.toItem(item);
            return AttributeModificationResult.MODIFIED_WAND_MODIFIER;
        }
    }

    public static <T> void modifyModifier(ItemStack item, SpellAttributeInstance<T> attributeInstance, AttributeModifierType modifierType, SpellModifier spellModifier) {
        SpellAttributeModifier<T, T> modifierInstance = attributeInstance.createModifier(modifierType);

        spellModifier.getModifiers().forEach(modifier -> {
            if (modifier.attribute().equals(modifierInstance.attribute())) {
                // Safe to remove in loop -- getModifiers returns copy
                spellModifier.removeModifier(modifier);
            }
        });
        spellModifier.addModifier(modifierInstance);

        spellModifier.toItem(item);
    }

    public static ItemStack buildEquipment(MagicEquipmentType magicEquipmentType) {
        return switch (magicEquipmentType) {
            case MagicHat hat -> buildHat(hat);
            default -> throw new IllegalStateException("Unexpected value: " + magicEquipmentType);
        };
    }

    @Nullable
    public static ItemStack buildItem(NamespacedKey key) {
        ItemType itemType = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).get(key);
        if (itemType != null) {
            return itemType.createItemStack();
        }
        // TODO: Move this to a more dynamic place that supports other one-offs like spellbooks etc
        if (key.equals(KEY_BLANK_SCROLL)) {
            return ItemUtils.buildBlankScroll();
        }

        List<ItemBuildableRegistry<?>> buildableRegistries = List.of(
                WandcraftRegistries.WAND_TYPES,
                WandcraftRegistries.MAGIC_EQUIPMENT_TYPES,
                WandcraftRegistries.SPELLS
        );

        for (ItemBuildableRegistry<?> registry : buildableRegistries) {
            ItemStack item = registry.getDefaultItem(key);
            if (item != null) {
                return item;
            }
        }

        return null;
    }

    public enum AttributeModificationResult {
        MODIFIED_MODIFIER,
        MODIFIED_WAND_MODIFIER,
        MODIFIED_WAND_ATTRIBUTE,
        MODIFIED_SPELL,
        INVALID_ITEM
    }
}
