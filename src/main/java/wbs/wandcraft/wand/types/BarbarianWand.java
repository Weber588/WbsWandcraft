package wbs.wandcraft.wand.types;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.wandcraft.RegisteredPersistentDataType;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.modifier.AttributeModifierType;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.definitions.extensions.RangedSpell;
import wbs.wandcraft.util.persistent.CustomPersistentDataTypes;
import wbs.wandcraft.wand.Wand;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@SuppressWarnings("UnstableApiUsage")
public class BarbarianWand extends Wand {
    public static final NamespacedKey ATTRIBUTE_KEY = WbsWandcraft.getKey("barbarian_range");
    private static final double PLAYER_DEFAULT_ENTITY_RANGE = 3;
    private static final double PLAYER_DEFAULT_BLOCK_RANGE = 4.5;
    public static final double BASE_RANGE = (PLAYER_DEFAULT_ENTITY_RANGE + PLAYER_DEFAULT_BLOCK_RANGE) / 2;

    private @Nullable ItemStack item;

    public BarbarianWand(@NotNull String uuid) {
        super(uuid);

        setAttribute(RangedSpell.RANGE, BASE_RANGE);
        setModifier(CastableSpell.COOLDOWN.createModifier(AttributeModifierType.MULTIPLY, RegisteredPersistentDataType.DOUBLE, 0.5));
    }

    @Override
    protected @NotNull Queue<@NotNull SpellInstance> getSpellQueue(@NotNull Player player, ItemStack wandItem, Event event) {
        LinkedList<@NotNull SpellInstance> spellList = new LinkedList<>();

        SpellInstance spellInstance = getSpellInstance();
        if (spellInstance != null) {
            SpellInstance modified = applyModifiers(spellInstance);

            if (modified.getDefinition() instanceof RangedSpell) {
                modified.setAttribute(RangedSpell.RANGE, this.getAttribute(RangedSpell.RANGE));
            }

            spellList.add(modified);
        }

        return spellList;
    }

    protected @Nullable SpellInstance getSpellInstance() {
        return SpellInstance.fromItem(item);
    }

    @Override
    public @NotNull BarbarianWandHolder getMenu(ItemStack item) {
        return new BarbarianWandHolder(this, item);
    }

    public @Nullable ItemStack getItem() {
        return item;
    }

    @Override
    public @NotNull WandType<BarbarianWand> getWandType() {
        return WandType.BARBARIAN;
    }

    @Override
    public @NotNull List<Component> getLore() {
        List<Component> lore = new LinkedList<>(super.getLore());

        Component spellText;
        SpellInstance spellInstance = getSpellInstance();
        if (spellInstance == null) {
            spellText = Component.text("None");
        } else {
            spellText = spellInstance.getDefinition().displayName();
        }

        lore.add(Component.text("Spell: ").color(NamedTextColor.AQUA)
                .append(spellText.color(NamedTextColor.GOLD)));

        return lore;
    }

    @Override
    public void toItem(ItemStack item) {
        super.toItem(item);
        item.editMeta(meta ->
                meta.getPersistentDataContainer().set(Wand.WAND_KEY, CustomPersistentDataTypes.BARBARIAN_WAND_TYPE, this)
        );

        double rangeBoost = getAttribute(RangedSpell.RANGE, 0d);

        for (SpellAttributeModifier<?, ?> modifier : attributeModifiers) {
            if (modifier.attribute().equals(RangedSpell.RANGE)) {
                //noinspection unchecked
                rangeBoost = ((SpellAttributeModifier<Double, ?>) modifier).modify(rangeBoost);
            }
        }

        if (rangeBoost > 0) {
            double entityRangeMultiplier = rangeBoost / PLAYER_DEFAULT_ENTITY_RANGE;
            double blockRangeMultiplier = rangeBoost / PLAYER_DEFAULT_BLOCK_RANGE;

            AttributeModifier entityModifier = new AttributeModifier(
                    ATTRIBUTE_KEY,
                    entityRangeMultiplier - 1,
                    AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                    EquipmentSlotGroup.MAINHAND
            );

            AttributeModifier blockModifier = new AttributeModifier(
                    ATTRIBUTE_KEY,
                    blockRangeMultiplier - 1,
                    AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                    EquipmentSlotGroup.MAINHAND
            );

            item.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS,
                    ItemAttributeModifiers.itemAttributes()
                            .addModifier(
                                    Attribute.ENTITY_INTERACTION_RANGE,
                                    entityModifier,
                                    EquipmentSlotGroup.MAINHAND
                            ).addModifier(
                                    Attribute.BLOCK_INTERACTION_RANGE,
                                    blockModifier,
                                    EquipmentSlotGroup.MAINHAND
                            )
                            .build()
            );

            item.setData(
                    DataComponentTypes.TOOLTIP_DISPLAY,
                    TooltipDisplay.tooltipDisplay()
                            .addHiddenComponents(DataComponentTypes.ATTRIBUTE_MODIFIERS)
                            .build()
            );
        } else {
            item.unsetData(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        }
    }

    public void setItem(@Nullable ItemStack newItem) {
        this.item = newItem;
    }

    @Override
    protected Color getWandColour() {
        SpellInstance spellInstance = getSpellInstance();
        if (spellInstance != null) {
            return spellInstance.getDefinition().getPrimarySpellType().wandColor();
        }
        return null;
    }

    // Prevent non-entity/block related events from causing a cast
    @Override
    public void handleRightClick(Player player, ItemStack item, PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        tryCasting(player, item, event);
    }
}
