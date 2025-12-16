package wbs.wandcraft.wand.types;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.Ticks;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.NotNull;
import wbs.wandcraft.spell.attributes.BooleanSpellAttribute;
import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.modifier.SpellModifier;
import wbs.wandcraft.util.persistent.CustomPersistentDataTypes;
import wbs.wandcraft.wand.Wand;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class WizardryWand extends Wand {
    public static final SpellAttribute<Boolean> SHUFFLE = new BooleanSpellAttribute("shuffle", false)
            .setShowAttribute(shuffle -> shuffle);
    public static final SpellAttribute<Integer> SLOTS = new IntegerSpellAttribute("slots", 10);
    public static final int ITEM_COLUMN_START = 3;
    public static final int ITEM_COLUMN_END = 7;
    public static final int ITEM_ROW_END = 4;
    public static final int ITEM_ROW_START = 1;
    public static final int FULL_INV_COLUMNS = 9;
    public static final int FULL_INV_ROWS = 6;

    private final Table<Integer, Integer, ItemStack> items = HashBasedTable.create();

    public WizardryWand(@NotNull String uuid) {
        super(uuid);
        setAttribute(SHUFFLE.defaultInstance());
        setAttribute(SLOTS.defaultInstance());
    }

    @Override
    protected int getAdditionalCooldown(@NotNull Player player, ItemStack wandItem) {
        Table<Integer, Integer, SpellInstance> spellTable = getModifiedSpellTable();

        int additionalCooldown = 0;

        for (SpellInstance spell : spellTable.values()) {
            additionalCooldown += (int) (spell.getAttribute(CastableSpell.COOLDOWN) * Ticks.SINGLE_TICK_DURATION_MS);
            additionalCooldown += (int) (spell.getAttribute(CastableSpell.DELAY) * Ticks.SINGLE_TICK_DURATION_MS);
        }

        return additionalCooldown;
    }

    @Override
    protected @NotNull Queue<SpellInstance> getSpellQueue(@NotNull Player player, ItemStack wandItem) {
        Table<Integer, Integer, SpellInstance> spellTable = getModifiedSpellTable();

        Queue<SpellInstance> spellList = new LinkedList<>();
        // Iterate column-first, so inner loop iterates per row (natural enqueue order)
        for (Map<Integer, SpellInstance> rowMap : spellTable.columnMap().values()) {
            spellList.addAll(rowMap.values()); // Order is maintained
        }
        return spellList;
    }

    @Override
    public @NotNull WizardryWandHolder getMenu(ItemStack item) {
        return new WizardryWandHolder(this, item);
    }

    private Table<Integer, Integer, SpellInstance> getRawSpellTable() {
        HashBasedTable<Integer, Integer, SpellInstance> table = HashBasedTable.create();

        Boolean shuffle = getAttribute(SHUFFLE);
        if (shuffle != null && shuffle) {
            LinkedList<SpellInstance> spellList = new LinkedList<>();
            for (ItemStack item : items.values()) {
                SpellInstance instance = SpellInstance.fromItem(item);
                if (instance != null) {
                    spellList.add(instance);
                } else if (SpellModifier.fromItem(item) != null) {
                    spellList.add(null);
                }
            }

            Collections.shuffle(spellList);

            for (int row = 0; row < FULL_INV_ROWS; row++) {
                for (int column = 0; column < FULL_INV_COLUMNS; column++) {
                    SpellInstance instance = spellList.poll();
                    if (instance != null) {
                        table.put(row, column, instance);
                    }
                }
            }
        } else {
            for (int row = 0; row < FULL_INV_ROWS; row++) {
                for (int column = 0; column < FULL_INV_COLUMNS; column++) {
                    ItemStack item = items.get(row, column);
                    if (item != null) {
                        SpellInstance instance = SpellInstance.fromItem(item);
                        if (instance != null) {
                            table.put(row, column, instance);
                        }
                    }
                }
            }
        }

        return table;
    }

    private Table<Integer, Integer, SpellModifier> getModifierTable() {
        HashBasedTable<Integer, Integer, SpellModifier> table = HashBasedTable.create();

        Boolean shuffle = getAttribute(SHUFFLE);
        if (shuffle != null && shuffle) {
            LinkedList<SpellModifier> modifierList = new LinkedList<>();
            for (ItemStack item : items.values()) {
                SpellModifier modifier = SpellModifier.fromItem(item);
                if (modifier != null) {
                    modifierList.add(modifier);
                } else if (SpellInstance.fromItem(item) != null) {
                    modifierList.add(null);
                }
            }

            Collections.shuffle(modifierList);

            for (int row = 0; row < FULL_INV_ROWS; row++) {
                for (int column = 0; column < FULL_INV_COLUMNS; column++) {
                    SpellModifier modifier = modifierList.poll();
                    if (modifier != null) {
                        table.put(row, column, modifier);
                    }
                }
            }
        } else {
            for (int row = 0; row < FULL_INV_ROWS; row++) {
                for (int column = 0; column < FULL_INV_COLUMNS; column++) {
                    ItemStack item = items.get(row, column);
                    if (item != null) {
                        SpellModifier modifier = SpellModifier.fromItem(item);
                        if (modifier != null) {
                            table.put(row, column, modifier);
                        }
                    }
                }
            }
        }

        return table;
    }

    private Table<Integer, Integer, SpellInstance> getModifiedSpellTable() {
        Table<Integer, Integer, SpellInstance> spellTable = getRawSpellTable();

        // Apply wand modifiers first
        spellTable.rowMap().forEach((column, columnMap) -> {
            columnMap.values().forEach(instance -> attributeModifiers.forEach(modifier -> modifier.modify(instance)));
        });

        Table<Integer, Integer, SpellModifier> modifierTable = getModifierTable();
        for (int row = 0; row < 9; row++) {
            Map<Integer, SpellModifier> rowMap = modifierTable.row(row);

            int finalRow = row;
            rowMap.forEach((column, modifier) -> modifier.modify(spellTable, finalRow, column));
        }

        return spellTable;
    }

    @Override
    public boolean canContain(@NotNull ItemStack addedItem) {
        return super.canContain(addedItem) || SpellModifier.fromItem(addedItem) != null;
    }

    @Override
    public boolean isItemSlot(int slot) {
        int row = slot / FULL_INV_COLUMNS;
        int column = slot % FULL_INV_COLUMNS;

        return row >= ITEM_ROW_START &&
                row <= ITEM_ROW_END &&
                column >= ITEM_COLUMN_START &&
                column <= ITEM_COLUMN_END
                ;
    }

    public Table<Integer, Integer, ItemStack> getItems() {
        return items;
    }

    // TODO: Move this to wand holder
    @Override
    public void updateItems(Inventory inventory) {
        // TODO: Make upgrade slots save somewhere
        for (int column = ITEM_COLUMN_START; column < ITEM_COLUMN_END; column++) {
            for (int row = ITEM_ROW_START; row < ITEM_ROW_END; row++) {
                int slot = row * FULL_INV_COLUMNS + column;
                ItemStack item = inventory.getItem(slot);
                if (item != null && canContain(item)) {
                    items.put(row, column, item);
                } else {
                    items.remove(row, column);
                }
            }
        }
    }

    // TODO: Make this configurable
    @Override
    public @NotNull Component getItemName() {
        return Component.text("Wizardry Wand");
    }

    @Override
    public @NotNull WandType<WizardryWand> getWandType() {
        return WandType.WIZARDRY;
    }

    @Override
    public void toContainer(PersistentDataContainer container) {
        getWandType().toContainer(this, container);
    }

    @Override
    public @NotNull List<Component> getLore() {
        List<Component> lore = new LinkedList<>(super.getLore());

        Table<Integer, Integer, SpellInstance> rawSpellTable = getRawSpellTable();
        if (rawSpellTable.isEmpty()) {
            lore.add(Component.text("Spells:").color(NamedTextColor.AQUA).append(Component.text(" None").color(NamedTextColor.GOLD)));
        } else {
            lore.add(Component.text("Spells:").color(NamedTextColor.AQUA));

            rawSpellTable.columnMap().forEach((column, rowMap) -> {
                rowMap.forEach((row, instance) -> {
                    lore.add(Component.text("  - ").color(NamedTextColor.GOLD)
                            .append(
                                    instance.getDefinition().displayName()
                            ));
                });
            });
        }

        return lore;
    }

    @Override
    public void toItem(ItemStack item) {
        super.toItem(item);
        item.editMeta(meta ->
                meta.getPersistentDataContainer().set(Wand.WAND_KEY, CustomPersistentDataTypes.WIZARDRY_WAND_TYPE, this)
        );
    }
}
