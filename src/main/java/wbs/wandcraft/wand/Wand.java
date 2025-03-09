package wbs.wandcraft.wand;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.papermc.paper.persistence.PersistentDataContainerView;
import io.papermc.paper.util.Tick;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import wbs.utils.util.persistent.WbsPersistentDataType;
import wbs.utils.util.string.WbsStringify;
import wbs.wandcraft.ItemDecorator;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.Attributable;
import wbs.wandcraft.spell.attributes.IntegerSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.modifier.SpellModifier;
import wbs.wandcraft.util.CustomPersistentDataTypes;

import java.time.Duration;
import java.util.*;

public class Wand implements Attributable, ItemDecorator {
    public static final NamespacedKey WAND_KEY = WbsWandcraft.getKey("wand");
    private static final NamespacedKey LAST_USED = WbsWandcraft.getKey("last_used");

    public static final SpellAttribute<Integer> WAND_COOLDOWN = new IntegerSpellAttribute("wand_cooldown", 0, 40);
    public static final SpellAttribute<Integer> WAND_CAST_DELAY = new IntegerSpellAttribute("wand_cast_delay", 0, 10);

    @Nullable
    public static Wand getIfValid(ItemStack item) {
        return item.getPersistentDataContainer().get(WAND_KEY, CustomPersistentDataTypes.WAND);
    }

    public static void updateLastUsed(int additionalCooldown, PersistentDataContainer container) {
        container.set(LAST_USED, PersistentDataType.INTEGER, Bukkit.getCurrentTick() - additionalCooldown);
    }

    public static int getLastUsed(PersistentDataContainerView container) {
        return WbsPersistentDataType.getOrDefault(container, LAST_USED, PersistentDataType.INTEGER, 0);
    }

    private final Table<Integer, Integer, ItemStack> items = HashBasedTable.create();
    private final WandInventoryType type;
    private final Set<SpellAttributeInstance<?>> attributeValues = new HashSet<>();

    public Wand(WandInventoryType type) {
        this.type = type;
        setAttribute(WAND_COOLDOWN.getInstance());
        setAttribute(WAND_CAST_DELAY.getInstance());
    }

    public Set<SpellAttributeInstance<?>> getAttributeValues() {
        return attributeValues;
    }

    public void cast(Player player, ItemStack item) {
        item.editMeta(meta -> {
            cast(player, meta.getPersistentDataContainer());
        });
    }

    public void cast(Player player, PersistentDataContainer wandContainer) {
        int lastUsed = getLastUsed(wandContainer);
        int usableTick = lastUsed + getAttribute(WAND_COOLDOWN);
        if (Bukkit.getCurrentTick() <= usableTick) {
            Duration timeLeft = Tick.of(usableTick - Bukkit.getCurrentTick());
            String timeLeftString = WbsStringify.toString(timeLeft, false);

            WbsWandcraft.getInstance().sendActionBar("You can use that again in " + timeLeftString, player);
            return;
        }

        Table<Integer, Integer, SpellInstance> spellTable = getModifiedSpellTable();

        int additionalCooldown = 0;

        Queue<SpellInstance> spellList = new LinkedList<>();
        // Iterate column-first, so inner loop iterates per row (natural enqueue order)
        for (Map<Integer, SpellInstance> rowMap : spellTable.columnMap().values()) {
            for (SpellInstance spell : rowMap.values()) {
                spellList.add(spell);
                additionalCooldown += spell.getAttribute(CastableSpell.COOLDOWN);
            }
        }

        enqueueCast(player, spellList);
        updateLastUsed(additionalCooldown, wandContainer);
    }

    private void enqueueCast(Player player, Queue<SpellInstance> instances) {
        SpellInstance toCast = instances.poll();

        if (toCast == null) {
            return;
        }

        toCast.cast(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                enqueueCast(player, instances);
            }
        }.runTaskLater(WbsWandcraft.getInstance(), toCast.getAttribute(CastableSpell.DELAY) + getAttribute(WAND_CAST_DELAY));
    }
    public @NotNull WandHolder getInventory(ItemStack item) {
        return new WandHolder(this, item);
    }

    public @NotNull Inventory getInventory(WandHolder holder) {
        Inventory inventory = type.newInventory(holder);

        items.rowMap().forEach((row, map) -> {
            map.forEach((column, item) -> {
                inventory.setItem(row * type.getColumns() + column, item);
            });
        });

        return inventory;
    }

    public Table<Integer, Integer, SpellInstance> getRawSpellTable() {
        HashBasedTable<Integer, Integer, SpellInstance> table = HashBasedTable.create();
        for (int row = 0; row < type.getRows(); row++) {
            for (int column = 0; column < type.getColumns(); column++) {
                ItemStack item = items.get(row, column);
                if (item != null) {
                    SpellInstance instance = SpellInstance.fromItem(item);
                    if (instance != null) {
                        table.put(row, column, instance);
                    }
                }
            }
        }

        return table;
    }

    public Table<Integer, Integer, SpellModifier> getModifierTable() {
        HashBasedTable<Integer, Integer, SpellModifier> table = HashBasedTable.create();
        for (int row = 0; row < type.getRows(); row++) {
            for (int column = 0; column < type.getColumns(); column++) {
                ItemStack item = items.get(row, column);
                if (item != null) {
                    SpellModifier modifier = SpellModifier.fromItem(item);
                    if (modifier != null) {
                        table.put(row, column, modifier);
                    }
                }
            }
        }

        return table;
    }

    public Table<Integer, Integer, SpellInstance> getModifiedSpellTable() {
        Table<Integer, Integer, SpellInstance> spellTable = getRawSpellTable();

        Table<Integer, Integer, SpellModifier> modifierTable = getModifierTable();
        for (int row = 0; row < type.getRows(); row++) {
            Map<Integer, SpellModifier> rowMap = modifierTable.row(row);

            int finalRow = row;
            rowMap.forEach((column, modifier) -> modifier.modify(spellTable, finalRow, column, type));
        }

        return spellTable;
    }

    public WandInventoryType getInventoryType() {
        return type;
    }

    public boolean canContain(ItemStack addedItem) {
        SpellInstance instance = addedItem.getPersistentDataContainer().get(SpellInstance.SPELL_INSTANCE_KEY, CustomPersistentDataTypes.SPELL_INSTANCE);
        SpellModifier spellModifier = addedItem.getPersistentDataContainer().get(SpellModifier.SPELL_MODIFIER_KEY, CustomPersistentDataTypes.SPELL_MODIFIER);

        return instance != null || spellModifier != null;
    }

    public Table<Integer, Integer, ItemStack> getItems() {
        return items;
    }

    public void updateItems(Inventory inventory) {
        for (int row = 0; row < type.getRows(); row++) {
            for (int column = 0; column < type.getColumns(); column++) {
                int slot = row * type.getColumns() + column;
                ItemStack item = inventory.getItem(slot);
                if (item != null) {
                    items.put(row, column, item);
                }
            }
        }
    }
}
