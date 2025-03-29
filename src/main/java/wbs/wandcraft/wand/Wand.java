package wbs.wandcraft.wand;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import io.papermc.paper.persistence.PersistentDataContainerView;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import wbs.wandcraft.WandcraftRegistries;
import wbs.wandcraft.WbsWandcraft;
import wbs.wandcraft.spell.attributes.Attributable;
import wbs.wandcraft.spell.attributes.LongSpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttribute;
import wbs.wandcraft.spell.attributes.SpellAttributeInstance;
import wbs.wandcraft.spell.attributes.modifier.AttributeAddModifierType;
import wbs.wandcraft.spell.attributes.modifier.SpellAttributeModifier;
import wbs.wandcraft.spell.definitions.SpellInstance;
import wbs.wandcraft.spell.definitions.extensions.CastableSpell;
import wbs.wandcraft.spell.modifier.SpellModifier;
import wbs.wandcraft.util.CustomPersistentDataTypes;

import java.time.Duration;
import java.util.*;

public class Wand implements Attributable {
    public static final NamespacedKey WAND_KEY = WbsWandcraft.getKey("wand");
    private static final NamespacedKey LAST_USED = WbsWandcraft.getKey("last_used");

    public static final SpellAttribute<Long> COOLDOWN = new LongSpellAttribute("wand_cooldown", 0, 40)
            .setFormatter(cooldown -> cooldown / 20.0 + " seconds");

    @Nullable
    public static Wand getIfValid(ItemStack item) {
        return item.getPersistentDataContainer().get(WAND_KEY, CustomPersistentDataTypes.WAND);
    }

    public static void updateLastUsed(PersistentDataContainer container) {
        long currentTick = getTimestamp();
        container.set(LAST_USED, PersistentDataType.LONG, currentTick);
    }

    private static long getTimestamp() {
        return System.currentTimeMillis();
    }

    public static long getLastUsed(PersistentDataContainerView container) {
        return WbsPersistentDataType.getOrDefault(container, LAST_USED, PersistentDataType.LONG, 0L);
    }

    private final Table<Integer, Integer, ItemStack> items = HashBasedTable.create();
    private final WandInventoryType type;
    private final Set<SpellAttributeInstance<?>> attributeValues = new HashSet<>();
    private final Set<SpellAttributeModifier<?>> attributeModifiers = new HashSet<>();

    public Wand(WandInventoryType type) {
        this.type = type;
        addAttribute(COOLDOWN.defaultInstance());

        // TODO: Move these defaults to config
        SpellAttributeModifier<Integer> defaultDelayModifier = new SpellAttributeModifier<>(CastableSpell.DELAY,
                WandcraftRegistries.MODIFIER_TYPES.get(AttributeAddModifierType.KEY),
                10);

        attributeModifiers.add(defaultDelayModifier);
    }

    public Set<SpellAttributeInstance<?>> getAttributeValues() {
        return attributeValues;
    }

    public void startCasting(Player player, ItemStack item) {
        item.editMeta(meta -> {
            PersistentDataContainer wandContainer = meta.getPersistentDataContainer();

            Table<Integer, Integer, SpellInstance> spellTable = getModifiedSpellTable();

            int additionalCooldown = 0;

            Queue<SpellInstance> spellList = new LinkedList<>();
            // Iterate column-first, so inner loop iterates per row (natural enqueue order)
            for (Map<Integer, SpellInstance> rowMap : spellTable.columnMap().values()) {
                for (SpellInstance spell : rowMap.values()) {
                    spellList.add(spell);
                    additionalCooldown += spell.getAttribute(CastableSpell.COOLDOWN) * 1000 / 20;
                    additionalCooldown += spell.getAttribute(CastableSpell.DELAY) * 1000 / 20;
                }
            }

            long lastUsed = getLastUsed(wandContainer);
            long usableTick = lastUsed + getAttribute(COOLDOWN) * 1000 / 20 + additionalCooldown;
            long timestamp = getTimestamp();
            if (timestamp <= usableTick) {
                Duration timeLeft = Duration.ofMillis(usableTick - timestamp);
                String timeLeftString = WbsStringify.toString(timeLeft, false);

                WbsWandcraft.getInstance().sendActionBar("You can use that again in " + timeLeftString, player);
                return;
            }

            enqueueCast(player, spellList, item);
            updateLastUsed(wandContainer);
            player.setCooldown(item, additionalCooldown * 20 / 1000);
        });
    }

    private void enqueueCast(Player player, Queue<SpellInstance> instances, ItemStack item) {
        if (!player.getInventory().getItemInMainHand().equals(item)) {
            return;
        }

        SpellInstance toCast = instances.poll();

        if (toCast == null) {
            return;
        }

        toCast.cast(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                enqueueCast(player, instances, item);
            }
        }.runTaskLater(WbsWandcraft.getInstance(), toCast.getAttribute(CastableSpell.DELAY));
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

        // Apply wand modifiers first
        spellTable.rowMap().forEach((column, columnMap) -> {
            columnMap.values().forEach(instance -> attributeModifiers.forEach(modifier -> modifier.modify(instance)));
        });

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
        SpellInstance instance = SpellInstance.fromItem(addedItem);
        SpellModifier spellModifier = SpellModifier.fromItem(addedItem);

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
                } else {
                    items.remove(row, column);
                }
            }
        }
    }

    @Override
    public @NotNull List<Component> getLore() {
        List<Component> lore = new LinkedList<>();

        lore.add(Component.text("Attributes:").color(NamedTextColor.AQUA));
        lore.addAll(Attributable.super.getLore());

        if (!attributeModifiers.isEmpty()) {
            lore.add(Component.text("Modifiers:").color(NamedTextColor.AQUA));

            // TODO: Unify this code with code from SpellModifier
            lore.addAll(attributeModifiers.stream()
                    .map(modifier ->
                            (Component) Component.text("  - ").color(NamedTextColor.GOLD)
                                    .append(modifier.toComponent())
                    )
                    .toList());
        }

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

    public void toItem(ItemStack item) {
        item.editMeta(meta -> {
            meta.getPersistentDataContainer().set(Wand.WAND_KEY, CustomPersistentDataTypes.WAND, this);
            ItemDecorator.decorate(this, meta);
        });
    }

    public void setModifier(SpellAttributeModifier<?> updatedModifier) {
        attributeModifiers.removeIf(modifier -> modifier.attribute().equals(updatedModifier.attribute()));

        attributeModifiers.add(updatedModifier);
    }
}
